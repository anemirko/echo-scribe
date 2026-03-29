package com.nemirko.echoscribe.infra.download;

import java.nio.file.Path;

public class DownloadedMedia {

    private final Path mediaFile;
    private final Path workingDirectory;

    public DownloadedMedia(Path mediaFile, Path workingDirectory) {
        this.mediaFile = mediaFile;
        this.workingDirectory = workingDirectory;
    }

    public Path mediaFile() {
        return mediaFile;
    }

    public Path workingDirectory() {
        return workingDirectory;
    }
}
