# Architecture

EchoScribe is intentionally small and still avoids databases, but transcription runs asynchronously. HTTP requests enqueue jobs, return a job identifier immediately, and background workers execute the workflow while clients poll for status. This keeps the UI responsive for multi-minute inputs without introducing external queues.

## High-level design
- **Controllers** only coordinate HTTP-level concerns and delegate to services.
- **Services** host orchestration logic: acquisition, preparation, transcription, diagnostics, and the in-memory job queue.
- **Infrastructure components** wrap external processes (downloader, ffprobe, ffmpeg, whisper-cli) and expose narrow interfaces.
- **Configuration** is strongly typed using `@ConfigurationProperties`.
- **Templates** are logic-free views that render forms and results.

## Processing pipeline
### File uploads
1. User uploads a media file.
2. `MediaAcquisitionService` stores it under the configured temp directory and returns an `AcquiredMedia` that cleans up on close.
3. `TranscriptionJobService` registers a job, schedules work on the `transcriptionTaskExecutor`, and immediately returns the job id.
4. The worker invokes `MediaPreparationService` (ffprobe/ffmpeg) followed by `WhisperExecutor` to run `whisper-cli`.
5. `TranscriptionService` assembles the `TranscriptionResult`, which is stored on the job record.
6. Temp files are deleted automatically when the job finishes.

### URL inputs
1. User provides a media URL.
2. The controller validates basic input immediately; `TranscriptionJobService` enqueues a job.
3. In the worker thread, `UrlMediaDownloadService` validates again, downloads the media (with short-lived caching), and hands the file to the same pipeline as an uploaded file.
4. The download directory is cleaned up with the rest of the temp artifacts once the job completes.

## Dependency diagnostics
`ExternalToolDiagnosticsService` runs `--version` checks for `ffmpeg`, `ffprobe`, `whisper-cli`, and the configured downloader (if URL input is enabled). It also verifies the Whisper model path is readable. `SystemStatusService` aggregates the check list into a readiness summary for file and URL workflows. Both the UI and `/api/system/status` expose the results so missing dependencies are visible without blocking startup.

## Why an in-memory queue?
The previous fully synchronous approach timed out with multi-hour media. A lightweight in-memory queue plus polling gives users responsive APIs without adding external dependencies. Jobs are not persisted across restarts, which keeps the system stateless and easy to run locally while still tolerating long whisper runs.

## External native tooling
Media parsing, conversion, downloading, and transcription are delegated to battle-tested CLI tools instead of Java libraries. This keeps the application lightweight, enables users to swap tools easily, and avoids bundling heavy multimedia frameworks.
