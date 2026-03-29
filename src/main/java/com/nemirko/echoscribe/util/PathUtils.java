package com.nemirko.echoscribe.util;

import java.nio.file.Path;

public final class PathUtils {

    private PathUtils() {
    }

    public static Path expandUserHome(String rawPath) {
        if (rawPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (!rawPath.startsWith("~")) {
            return Path.of(rawPath);
        }
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) {
            return Path.of(rawPath);
        }
        if (rawPath.length() == 1) {
            return Path.of(home);
        }
        char separator = rawPath.charAt(1);
        if (separator == '/' || separator == '\\') {
            String remainder = rawPath.substring(2);
            if (remainder.isEmpty()) {
                return Path.of(home);
            }
            return Path.of(home).resolve(remainder);
        }
        return Path.of(rawPath);
    }
}
