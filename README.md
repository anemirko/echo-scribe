# EchoScribe

EchoScribe is a minimal Spring Boot web application that performs synchronous speech transcription locally by orchestrating `ffmpeg`, `ffprobe`, and `whisper.cpp`. Users can either upload a media file or provide a supported media URL (downloaded through `yt-dlp` or another CLI), and the application returns the transcription.

## What it does
- Accepts media inputs through file uploads or supported URLs
- Probes media with `ffprobe`, prepares audio with `ffmpeg`, and transcribes with `whisper-cli`
- Provides both a simple Thymeleaf UI and JSON APIs
- Performs system diagnostics for external tool availability and Whisper model readiness
- Logs operational events to console and rolling log files

## What it does not do
- No authentication, databases, job queues, or background workers
- No subtitle editing, diarization, translation, or batching
- No persistence beyond temporary files

## Supported input types
- Local audio/video files uploaded through the UI or API (`multipart/form-data`)
- Media URLs resolvable by the configured downloader (default `yt-dlp`) such as YouTube links

## External dependencies
- Required: `ffmpeg`, `ffprobe`, `whisper-cli` (from whisper.cpp), and a Whisper model file readable by the application
- Optional: `yt-dlp` (or another downloader CLI) for URL handling; disable URL input if unavailable

## Configuration
All configuration lives in `application.yml` using typed properties under `app.transcription`.

```yaml
app:
  transcription:
    ffmpeg-command: ffmpeg
    ffprobe-command: ffprobe
    whisper-command: whisper-cli
    whisper-model-path: /path/to/ggml-base.bin
    temp-dir: build/tmp/echo-scribe
    url-input-enabled: true
    downloader-command: yt-dlp
    request-timeout-seconds: 300
    download-cache-ttl: 12h
  logging:
    directory: logs
```

Set `whisper-model-path` to a real, readable model file and adjust the CLI command names if they are not on your `PATH`. The `temp-dir` should be writable; temporary files are created and cleaned up there.
Repeated URL downloads are cached under `temp-dir/cache/url-downloads` for `download-cache-ttl` (12 hours by default); set the TTL to `0s` to disable caching.

## Running locally
1. Install the required native tools (`ffmpeg`, `ffprobe`, `whisper-cli`, Whisper model). Homebrew works well on macOS.
2. Configure `application.yml` with the correct model path and tool names.
3. Build and run:
   ```bash
   ./gradlew bootRun
   ```
4. Open `http://localhost:8080` for the UI or use the API endpoints described below.

## Using the UI
The home page (`/`) shows:
- System readiness summary
- File transcription form
- URL transcription form
- Result area showing language, duration, and transcript

A dedicated diagnostics page is available at `/system/status`.

## API usage
- `GET /api/system/status` – returns readiness information and dependency check results
- `POST /api/transcriptions/file` (`multipart/form-data`) – field `file` with the media file, optional `language`
- `POST /api/transcriptions/url` (`application/json`) – body `{ "url": "https://...", "language": "en" }`

Responses include:
```json
{
  "sourceType": "FILE_UPLOAD",
  "sourceName": "meeting.mp4",
  "sourceUrl": null,
  "detectedLanguage": "en",
  "transcriptionText": "...",
  "duration": "PT1M3S"
}
```
Errors are returned as RFC 7807 `ProblemDetail` payloads with actionable messages when tools are missing or inputs are invalid.

## Logging
Logs go to console and to `app.logging.directory` (`logs` by default) with daily size-based rolling files. Key pipeline stages are logged without dumping the full transcript.

## Known limitations
- Processing is synchronous and single-request; long-running transcriptions block the request thread.
- No progress updates or streaming responses.
- URL support depends entirely on the configured downloader CLI.
- Whisper output text files are read entirely into memory.

For deeper architectural notes, see `docs/architecture.md`. Status, limitations, and testing strategy live under `docs/` as well.
