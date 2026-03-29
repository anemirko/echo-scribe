package com.nemirko.echoscribe.dto;

import jakarta.validation.constraints.NotBlank;

public class UrlTranscriptionRequest {

    @NotBlank
    private String url;

    private String language;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
