package com.nemirko.echoscribe.infra.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PreparedAudio implements AutoCloseable {

    private final Path audioFile;
    private final boolean deleteOnClose;

    public PreparedAudio(Path audioFile, boolean deleteOnClose) {
        this.audioFile = audioFile;
        this.deleteOnClose = deleteOnClose;
    }

    public Path audioFile() {
        return audioFile;
    }

    @Override
    public void close() {
        if (!deleteOnClose) {
            return;
        }
        try {
            Files.deleteIfExists(audioFile);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }
}
