package com.nemirko.echoscribe.infra.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultExternalCommandExecutor implements ExternalCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultExternalCommandExecutor.class);

    private final ExecutorService ioExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "command-stream-" + System.nanoTime());
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    public void shutdown() {
        ioExecutor.shutdownNow();
    }

    @Override
    public CommandResult run(CommandRequest request) {
        List<String> command = request.command();
        ProcessBuilder builder = new ProcessBuilder(command);
        if (request.workingDirectory() != null) {
            builder.directory(request.workingDirectory().toFile());
        }
        if (!request.environment().isEmpty()) {
            builder.environment().putAll(request.environment());
        }

        try {
            Process process = builder.start();
            Future<String> stdout = ioExecutor.submit(new StreamCollector(process.getInputStream()));
            Future<String> stderr = ioExecutor.submit(new StreamCollector(process.getErrorStream()));

            boolean completed = process.waitFor(timeoutMillis(request.timeout()), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new CommandExecutionException("Command timed out: " + String.join(" ", command));
            }

            int exitCode = process.exitValue();
            String out = stdout.get();
            String err = stderr.get();
            log.debug("Command {} exited with {}", command, exitCode);
            return new CommandResult(exitCode, out, err);
        } catch (CommandExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandExecutionException("Failed to run command: " + String.join(" ", command), e);
        }
    }

    private long timeoutMillis(Duration timeout) {
        return timeout == null ? TimeUnit.SECONDS.toMillis(60) : timeout.toMillis();
    }

    private static class StreamCollector implements Callable<String> {
        private final InputStream inputStream;

        private StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public String call() throws IOException {
            byte[] buffer = inputStream.readAllBytes();
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }
}
