package com.nemirko.echoscribe.dto;

import com.nemirko.echoscribe.model.TranscriptionResult;
import com.nemirko.echoscribe.model.TranscriptionSourceType;
import java.time.Duration;

public class TranscriptionResponse {

    private TranscriptionSourceType sourceType;
    private String sourceName;
    private String sourceUrl;
    private String detectedLanguage;
    private String transcriptionText;
    private Duration duration;

    public static TranscriptionResponse fromResult(TranscriptionResult result) {
        TranscriptionResponse response = new TranscriptionResponse();
        response.setSourceType(result.getSourceType());
        response.setSourceName(result.getSourceName());
        response.setSourceUrl(result.getSourceUrl());
        response.setDetectedLanguage(result.getLanguage());
        response.setTranscriptionText(result.getTranscriptionText());
        response.setDuration(result.getDuration());
        return response;
    }

    public TranscriptionSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(TranscriptionSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public void setTranscriptionText(String transcriptionText) {
        this.transcriptionText = transcriptionText;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
