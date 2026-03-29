package com.nemirko.echoscribe.dto;

import com.nemirko.echoscribe.model.TranscriptionJob;
import com.nemirko.echoscribe.model.TranscriptionJobStatus;
import java.time.Instant;

public class TranscriptionJobResponse {

    private String jobId;
    private TranscriptionJobStatus status;
    private TranscriptionResponse result;
    private String error;
    private Instant createdAt;
    private Instant updatedAt;

    public static TranscriptionJobResponse fromJob(TranscriptionJob job) {
        TranscriptionJobResponse response = new TranscriptionJobResponse();
        response.setJobId(job.getId());
        response.setStatus(job.getStatus());
        response.setResult(job.getResponse());
        response.setError(job.getError());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public TranscriptionJobStatus getStatus() {
        return status;
    }

    public void setStatus(TranscriptionJobStatus status) {
        this.status = status;
    }

    public TranscriptionResponse getResult() {
        return result;
    }

    public void setResult(TranscriptionResponse result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
