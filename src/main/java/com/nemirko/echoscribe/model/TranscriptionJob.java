package com.nemirko.echoscribe.model;

import com.nemirko.echoscribe.dto.TranscriptionResponse;
import java.time.Instant;
import java.util.UUID;

public class TranscriptionJob {

    private final String id;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    private volatile TranscriptionJobStatus status;
    private volatile TranscriptionResponse response;
    private volatile String error;

    public TranscriptionJob() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.status = TranscriptionJobStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public TranscriptionJobStatus getStatus() {
        return status;
    }

    public TranscriptionResponse getResponse() {
        return response;
    }

    public String getError() {
        return error;
    }

    public void markRunning() {
        this.status = TranscriptionJobStatus.RUNNING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted(TranscriptionResponse response) {
        this.status = TranscriptionJobStatus.SUCCEEDED;
        this.response = response;
        this.error = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = TranscriptionJobStatus.FAILED;
        this.error = message;
        this.updatedAt = Instant.now();
    }
}
