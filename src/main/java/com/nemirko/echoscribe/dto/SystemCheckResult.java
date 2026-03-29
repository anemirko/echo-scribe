package com.nemirko.echoscribe.dto;

public class SystemCheckResult {

    private final String name;
    private final boolean available;
    private final String message;

    public SystemCheckResult(String name, boolean available, String message) {
        this.name = name;
        this.available = available;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }
}
