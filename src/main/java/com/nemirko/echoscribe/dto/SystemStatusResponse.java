package com.nemirko.echoscribe.dto;

import java.util.List;

public class SystemStatusResponse {

    private boolean fileTranscriptionReady;
    private boolean urlTranscriptionReady;
    private List<SystemCheckResult> checks;

    public boolean isFileTranscriptionReady() {
        return fileTranscriptionReady;
    }

    public void setFileTranscriptionReady(boolean fileTranscriptionReady) {
        this.fileTranscriptionReady = fileTranscriptionReady;
    }

    public boolean isUrlTranscriptionReady() {
        return urlTranscriptionReady;
    }

    public void setUrlTranscriptionReady(boolean urlTranscriptionReady) {
        this.urlTranscriptionReady = urlTranscriptionReady;
    }

    public List<SystemCheckResult> getChecks() {
        return checks;
    }

    public void setChecks(List<SystemCheckResult> checks) {
        this.checks = checks;
    }
}
