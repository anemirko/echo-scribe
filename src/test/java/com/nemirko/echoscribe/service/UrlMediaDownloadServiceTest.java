package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.download.DownloadedMedia;
import com.nemirko.echoscribe.infra.download.DownloaderExecutor;
import com.nemirko.echoscribe.infra.process.CommandResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
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
        properties.setDownloadCacheTtl(Duration.ZERO);
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

    @Test
    void reusesDownloadWithinTtl(@TempDir Path workDir) throws IOException {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setTempDir(workDir.toString());
        properties.setDownloadCacheTtl(Duration.ofHours(12));
        DownloaderExecutor executor = Mockito.mock(DownloaderExecutor.class);
        when(executor.execute(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Path dir = invocation.getArgument(2);
            Files.writeString(dir.resolve("media.mp4"), "cached");
            return new CommandResult(0, "", "");
        });
        UrlMediaDownloadService service = new UrlMediaDownloadService(properties, executor);

        DownloadedMedia first = service.download("https://example.com/video");
        assertThat(first.mediaFile()).exists();

        DownloadedMedia second = service.download("https://example.com/video");
        assertThat(second.mediaFile()).exists();
        assertThat(Files.readString(second.mediaFile())).isEqualTo("cached");
        verify(executor, times(1)).execute(any(), any(), any(), any(), any());
    }

    @Test
    void redownloadsWhenCacheExpired(@TempDir Path workDir) throws IOException {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setTempDir(workDir.toString());
        properties.setDownloadCacheTtl(Duration.ofHours(12));
        DownloaderExecutor executor = Mockito.mock(DownloaderExecutor.class);
        AtomicInteger counter = new AtomicInteger();
        when(executor.execute(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Path dir = invocation.getArgument(2);
            int index = counter.incrementAndGet();
            Files.writeString(dir.resolve("media.mp4"), "data" + index);
            return new CommandResult(0, "", "");
        });
        UrlMediaDownloadService service = new UrlMediaDownloadService(properties, executor);

        DownloadedMedia first = service.download("https://example.com/video");
        assertThat(Files.readString(first.mediaFile())).isEqualTo("data1");

        Path cacheDir = workDir.resolve("cache").resolve("url-downloads");
        Path cachedFile;
        try (var files = Files.walk(cacheDir)) {
            cachedFile = files
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElseThrow();
        }
        Files.setLastModifiedTime(cachedFile, FileTime.from(Instant.now().minus(Duration.ofHours(13))));

        DownloadedMedia second = service.download("https://example.com/video");
        assertThat(Files.readString(second.mediaFile())).isEqualTo("data2");
        assertThat(counter.get()).isEqualTo(2);
    }
}
