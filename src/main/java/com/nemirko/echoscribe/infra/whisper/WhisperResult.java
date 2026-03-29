package com.nemirko.echoscribe.infra.whisper;

public class WhisperResult {

    private final String language;
    private final String transcript;

    public WhisperResult(String language, String transcript) {
        this.language = language;
        this.transcript = transcript;
    }

    public String language() {
        return language;
    }

    public String transcript() {
        return transcript;
    }
}
