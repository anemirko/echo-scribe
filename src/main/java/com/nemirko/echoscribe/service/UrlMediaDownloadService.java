package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.download.DownloadedMedia;
import com.nemirko.echoscribe.infra.download.DownloaderExecutor;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UrlMediaDownloadService {

    private static final Logger log = LoggerFactory.getLogger(UrlMediaDownloadService.class);

    private final TranscriptionProperties properties;
    private final DownloaderExecutor downloaderExecutor;

    public UrlMediaDownloadService(TranscriptionProperties properties,
                                   DownloaderExecutor downloaderExecutor) {
        this.properties = properties;
        this.downloaderExecutor = downloaderExecutor;
    }

    public DownloadedMedia download(String rawUrl) {
        if (!properties.isUrlInputEnabled()) {
            throw new InvalidRequestException("URL transcription is disabled by configuration");
        }
        URI uri = toUri(rawUrl);
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new InvalidRequestException("Only HTTP/HTTPS URLs are supported");
        }

        try {
            Path tempDir = Files.createDirectories(properties.tempDirPath());
            Path downloadDir = Files.createTempDirectory(tempDir, "download-");
            String outputTemplate = downloadDir.resolve("media.%(ext)s").toAbsolutePath().toString();

            log.info("Downloading media from {} into {}", uri, downloadDir);
            var result = downloaderExecutor.execute(
                    properties.getDownloaderCommand(),
                    uri.toString(),
                    downloadDir,
                    outputTemplate,
                    properties.requestTimeout());
            if (!result.isSuccess()) {
                throw new CommandExecutionException("Downloader failed: " + result.stderr());
            }
            Path downloadedFile;
            try (var files = Files.list(downloadDir)) {
                downloadedFile = files
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparingLong(this::lastModified))
                        .reduce((first, second) -> second)
                        .orElseThrow(() -> new CommandExecutionException("Downloader produced no file"));
            }
            return new DownloadedMedia(downloadedFile, downloadDir);
        } catch (IOException e) {
            throw new CommandExecutionException("Unable to store downloaded media", e);
        }
    }

    private URI toUri(String value) {
        if (!StringUtils.hasText(value)) {
            throw new InvalidRequestException("URL is required");
        }
        try {
            return new URI(value.trim());
        } catch (URISyntaxException e) {
            throw new InvalidRequestException("Invalid URL provided");
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
