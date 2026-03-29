package com.nemirko.echoscribe.model;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class AcquiredMedia implements AutoCloseable {

    private final TranscriptionSourceType sourceType;
    private final Path mediaPath;
    private final String displayName;
    private final String sourceUrl;
    private final List<Path> cleanupTargets = new ArrayList<>();

    public AcquiredMedia(TranscriptionSourceType sourceType,
                         Path mediaPath,
                         String displayName,
                         String sourceUrl) {
        this.sourceType = sourceType;
        this.mediaPath = mediaPath;
        this.displayName = displayName;
        this.sourceUrl = sourceUrl;
        this.cleanupTargets.add(mediaPath);
    }

    public TranscriptionSourceType getSourceType() {
        return sourceType;
    }

    public Path getMediaPath() {
        return mediaPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void registerCleanup(Path path) {
        cleanupTargets.add(path);
    }

    @Override
    public void close() {
        for (int i = cleanupTargets.size() - 1; i >= 0; i--) {
            Path path = cleanupTargets.get(i);
            deletePath(path);
        }
    }

    private void deletePath(Path path) {
        if (path == null) {
            return;
        }
        try {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
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
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }
}
