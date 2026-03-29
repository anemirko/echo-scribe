package com.nemirko.echoscribe.infra.download;

import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DownloaderExecutor {

    private final ExternalCommandExecutor commandExecutor;

    public DownloaderExecutor(ExternalCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public CommandResult execute(String downloaderCommand,
                                 String url,
                                 Path downloadDir,
                                 String outputTemplate,
                                 Duration timeout) {
        List<String> command = List.of(
                downloaderCommand,
                "--no-progress",
                "--output",
                outputTemplate,
                url);
        CommandRequest request = CommandRequest.builder(command)
                .timeout(timeout)
                .workingDirectory(downloadDir)
                .build();
        return commandExecutor.run(request);
    }
}
