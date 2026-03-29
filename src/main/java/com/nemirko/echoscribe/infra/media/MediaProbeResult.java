package com.nemirko.echoscribe.infra.media;

import java.time.Duration;

public class MediaProbeResult {

    private final String formatName;
    private final Duration duration;

    public MediaProbeResult(String formatName, Duration duration) {
        this.formatName = formatName;
        this.duration = duration;
    }

    public String formatName() {
        return formatName;
    }

    public Duration duration() {
        return duration;
    }

    public boolean isWavLike() {
        return formatName != null && formatName.toLowerCase().contains("wav");
    }
}
