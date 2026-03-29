package com.nemirko.echoscribe.infra.media;

import java.nio.file.Path;

public interface FfprobeExecutor {

    MediaProbeResult probe(Path mediaPath);
}
