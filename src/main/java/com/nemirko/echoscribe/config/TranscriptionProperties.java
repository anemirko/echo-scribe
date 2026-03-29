package com.nemirko.echoscribe.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.transcription")
public class TranscriptionProperties {

    private String ffmpegCommand = "ffmpeg";
    private String ffprobeCommand = "ffprobe";
    private String whisperCommand = "whisper-cli";
    private String whisperModelPath;
    private String tempDir = "build/tmp/echo-scribe";
    private boolean urlInputEnabled = true;
    private String downloaderCommand = "yt-dlp";
    private int requestTimeoutSeconds = 300;
    private Duration downloadCacheTtl = Duration.ofHours(12);
    private long languageDetectionSampleSeconds = 20;

    public String getFfmpegCommand() {
        return ffmpegCommand;
    }

    public void setFfmpegCommand(String ffmpegCommand) {
        this.ffmpegCommand = ffmpegCommand;
    }

    public String getFfprobeCommand() {
        return ffprobeCommand;
    }

    public void setFfprobeCommand(String ffprobeCommand) {
        this.ffprobeCommand = ffprobeCommand;
    }

    public String getWhisperCommand() {
        return whisperCommand;
    }

    public void setWhisperCommand(String whisperCommand) {
        this.whisperCommand = whisperCommand;
    }

    public String getWhisperModelPath() {
        return whisperModelPath;
    }

    public void setWhisperModelPath(String whisperModelPath) {
        this.whisperModelPath = whisperModelPath;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public boolean isUrlInputEnabled() {
        return urlInputEnabled;
    }

    public void setUrlInputEnabled(boolean urlInputEnabled) {
        this.urlInputEnabled = urlInputEnabled;
    }

    public String getDownloaderCommand() {
        return downloaderCommand;
    }

    public void setDownloaderCommand(String downloaderCommand) {
        this.downloaderCommand = downloaderCommand;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public Duration requestTimeout() {
        return Duration.ofSeconds(requestTimeoutSeconds);
    }

    public Duration getDownloadCacheTtl() {
        return downloadCacheTtl;
    }

    public void setDownloadCacheTtl(Duration downloadCacheTtl) {
        this.downloadCacheTtl = downloadCacheTtl;
    }

    public long getLanguageDetectionSampleSeconds() {
        return languageDetectionSampleSeconds;
    }

    public void setLanguageDetectionSampleSeconds(long languageDetectionSampleSeconds) {
        this.languageDetectionSampleSeconds = languageDetectionSampleSeconds;
    }

    public Path tempDirPath() {
        String configured = StringUtils.hasText(tempDir) ? tempDir : System.getProperty("java.io.tmpdir");
        return Paths.get(configured).toAbsolutePath();
    }
}
