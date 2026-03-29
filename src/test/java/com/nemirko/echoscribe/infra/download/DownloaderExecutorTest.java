package com.nemirko.echoscribe.infra.download;

import static org.assertj.core.api.Assertions.assertThat;

import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class DownloaderExecutorTest {

    @Test
    void buildsDownloaderCommand() {
        RecordingExecutor executor = new RecordingExecutor();
        DownloaderExecutor downloader = new DownloaderExecutor(executor);
        downloader.execute("yt-dlp", "https://example", Path.of("."), "output", Duration.ofSeconds(10));
        List<String> command = executor.lastRequest.command();
        assertThat(command.get(0)).isEqualTo("yt-dlp");
        assertThat(command).contains("https://example");
    }

    private static class RecordingExecutor implements ExternalCommandExecutor {
        private CommandRequest lastRequest;

        @Override
        public CommandResult run(CommandRequest request) {
            this.lastRequest = request;
            return new CommandResult(0, "", "");
        }
    }
}
