# Project Status

## Implemented
- File and URL transcription flows that share the same preparation/transcription pipeline
- External process wrappers for downloader, ffprobe, ffmpeg, and whisper-cli
- System diagnostics with both UI and API views
- Thymeleaf UI with upload forms, result rendering, and readiness summary
- JSON APIs returning structured transcription responses and ProblemDetail errors
- Logging configuration with console and rolling file appenders
- Comprehensive unit tests plus MockMvc integration tests
- Documentation for architecture, status, and testing

## Intentionally out of scope
- Persistent storage or databases
- Authentication/authorization
- Background job queues or async processing
- Progress indicators, streaming responses, or WebSockets
- Diarization, translation, or subtitle editing
- Cloud services or SaaS integrations

## Known limitations
- Requests are synchronous; a long transcription ties up the request thread
- URL downloads only work when the configured downloader CLI exists
- Whisper transcripts are loaded fully into memory before returning
- Temp files depend on the configured writable directory; cleanup is best-effort

## Future improvements (high-level)
- Add optional async workflow for long transcriptions
- Support multiple Whisper models or runtime selection
- Provide richer status UI and tooling auto-detection hints
