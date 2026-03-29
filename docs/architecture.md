# Architecture

EchoScribe is intentionally small and synchronous. Every HTTP request fully executes the transcription workflow before returning. This keeps the system stateless and easy to run locally while relying on native tools for heavy work.

## High-level design
- **Controllers** only coordinate HTTP-level concerns and delegate to services.
- **Services** host orchestration logic: acquisition, preparation, transcription, diagnostics.
- **Infrastructure components** wrap external processes (downloader, ffprobe, ffmpeg, whisper-cli) and expose narrow interfaces.
- **Configuration** is strongly typed using `@ConfigurationProperties`.
- **Templates** are logic-free views that render forms and results.

## Processing pipeline
### File uploads
1. User uploads a media file.
2. `MediaAcquisitionService` stores it under the configured temp directory and returns an `AcquiredMedia` that cleans up on close.
3. `MediaPreparationService` runs `ffprobe` to inspect the media. If not already a WAV, it invokes `ffmpeg` to extract mono 16 kHz audio.
4. `WhisperExecutor` launches `whisper-cli` with the configured model file and reads the generated transcript text.
5. `TranscriptionService` assembles the `TranscriptionResult` and returns it to controllers.
6. Temp files are deleted automatically after the request.

### URL inputs
1. User provides a media URL.
2. `UrlMediaDownloadService` validates the URL, then calls `DownloaderExecutor` (default `yt-dlp`) to download it into a temp directory.
3. The downloaded file enters the same pipeline as an uploaded file: `ffprobe` → `ffmpeg` (when needed) → `whisper-cli` → response.
4. The download directory is cleaned up with the rest of the temp artifacts.

## Dependency diagnostics
`ExternalToolDiagnosticsService` runs `--version` checks for `ffmpeg`, `ffprobe`, `whisper-cli`, and the configured downloader (if URL input is enabled). It also verifies the Whisper model path is readable. `SystemStatusService` aggregates the check list into a readiness summary for file and URL workflows. Both the UI and `/api/system/status` expose the results so missing dependencies are visible without blocking startup.

## Why synchronous and stateless?
The MVP prioritizes simplicity. Long-running requests are acceptable for the initial scope, and there is no queueing, persistence, or distributed coordination. This keeps the codebase focused on orchestration logic without introducing messaging systems or databases.

## External native tooling
Media parsing, conversion, downloading, and transcription are delegated to battle-tested CLI tools instead of Java libraries. This keeps the application lightweight, enables users to swap tools easily, and avoids bundling heavy multimedia frameworks.
