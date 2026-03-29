package com.nemirko.echoscribe.infra.media;

import java.nio.file.Path;

public interface FfmpegExecutor {

    PreparedAudio convertToWav(Path source, Path tempDir);
}
