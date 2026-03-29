package com.nemirko.echoscribe.model;

import java.time.Duration;

public class TranscriptionResult {

    private final TranscriptionSourceType sourceType;
    private final String sourceName;
    private final String sourceUrl;
    private final String language;
    private final String transcriptionText;
    private final Duration duration;

    public TranscriptionResult(
            TranscriptionSourceType sourceType,
            String sourceName,
            String sourceUrl,
            String language,
            String transcriptionText,
            Duration duration) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.language = language;
        this.transcriptionText = transcriptionText;
        this.duration = duration;
    }

    public TranscriptionSourceType getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getLanguage() {
        return language;
    }

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public Duration getDuration() {
        return duration;
    }
}
