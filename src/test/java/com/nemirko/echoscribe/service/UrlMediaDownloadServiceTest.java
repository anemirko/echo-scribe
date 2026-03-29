package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.download.DownloadedMedia;
import com.nemirko.echoscribe.infra.download.DownloaderExecutor;
import com.nemirko.echoscribe.infra.process.CommandResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class UrlMediaDownloadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsWhenDisabled() {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setUrlInputEnabled(false);
        UrlMediaDownloadService service = new UrlMediaDownloadService(properties, Mockito.mock(DownloaderExecutor.class));
        assertThatThrownBy(() -> service.download("https://example.com"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void failsOnInvalidUrl() {
        TranscriptionProperties properties = new TranscriptionProperties();
        UrlMediaDownloadService service = new UrlMediaDownloadService(properties, Mockito.mock(DownloaderExecutor.class));
        assertThatThrownBy(() -> service.download(":://bad"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void storesDownloadedFile(@TempDir Path workDir) throws IOException {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setTempDir(workDir.toString());
        DownloaderExecutor executor = Mockito.mock(DownloaderExecutor.class);
        when(executor.execute(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Path dir = invocation.getArgument(2);
            Files.writeString(dir.resolve("media.mp4"), "data");
            return new CommandResult(0, "", "");
        });
        UrlMediaDownloadService service = new UrlMediaDownloadService(properties, executor);
        DownloadedMedia media = service.download("https://example.com/video");
        assertThat(media.mediaFile()).exists();
        Files.deleteIfExists(media.mediaFile());
        Files.deleteIfExists(media.workingDirectory());
    }
}
