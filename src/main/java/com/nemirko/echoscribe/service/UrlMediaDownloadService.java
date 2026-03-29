package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.download.DownloadedMedia;
import com.nemirko.echoscribe.infra.download.DownloaderExecutor;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.stream.Stream;
import java.time.Duration;
import java.time.Instant;
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
            Duration cacheTtl = properties.getDownloadCacheTtl();
            Path tempDir = Files.createDirectories(properties.tempDirPath());
            Path cacheRoot = cacheRoot(tempDir, cacheTtl);
            Path downloadDir = Files.createTempDirectory(tempDir, "download-");
            String outputTemplate = downloadDir.resolve("media.%(ext)s").toAbsolutePath().toString();

            if (cacheRoot != null) {
                Path cachedCopy = tryCopyFromCache(cacheRoot, uri, cacheTtl, downloadDir);
                if (cachedCopy != null) {
                    return new DownloadedMedia(cachedCopy, downloadDir);
                }
            }

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
            if (cacheRoot != null) {
                storeInCache(cacheRoot, uri, downloadedFile);
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

    private Path cacheRoot(Path tempDir, Duration cacheTtl) throws IOException {
        if (!isCacheEnabled(cacheTtl)) {
            return null;
        }
        Path cacheDirectory = tempDir.resolve("cache").resolve("url-downloads");
        Files.createDirectories(cacheDirectory);
        return cacheDirectory;
    }

    private boolean isCacheEnabled(Duration ttl) {
        return ttl != null && !ttl.isZero() && !ttl.isNegative();
    }

    private Path tryCopyFromCache(Path cacheRoot, URI uri, Duration cacheTtl, Path downloadDir) throws IOException {
        Path cachedFile = findCachedFile(cacheRoot, uri, cacheTtl);
        if (cachedFile == null) {
            return null;
        }
        Path target = downloadDir.resolve(cachedFile.getFileName().toString());
        Files.copy(cachedFile, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Reusing cached download for {}", uri);
        return target;
    }

    private Path findCachedFile(Path cacheRoot, URI uri, Duration cacheTtl) throws IOException {
        Path entryDir = cacheRoot.resolve(cacheKey(uri));
        if (!Files.isDirectory(entryDir)) {
            return null;
        }
        try (Stream<Path> files = Files.list(entryDir)) {
            Path cachedFile = files.filter(Files::isRegularFile).findFirst().orElse(null);
            if (cachedFile == null) {
                deleteRecursively(entryDir);
                return null;
            }
            Instant lastModified = Files.getLastModifiedTime(cachedFile).toInstant();
            if (Instant.now().minus(cacheTtl).isAfter(lastModified)) {
                deleteRecursively(entryDir);
                return null;
            }
            return cachedFile;
        }
    }

    private void storeInCache(Path cacheRoot, URI uri, Path downloadedFile) {
        Path entryDir = cacheRoot.resolve(cacheKey(uri));
        try {
            deleteRecursively(entryDir);
            Files.createDirectories(entryDir);
            Path cacheFile = entryDir.resolve(downloadedFile.getFileName().toString());
            Files.copy(downloadedFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            Files.setLastModifiedTime(cacheFile, FileTime.from(Instant.now()));
        } catch (IOException e) {
            log.warn("Failed to store cached download for {}: {}", uri, e.getMessage());
        }
    }

    private void deleteRecursively(Path target) throws IOException {
        if (target == null || !Files.exists(target)) {
            return;
        }
        Files.walkFileTree(target, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String cacheKey(URI uri) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(uri.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
