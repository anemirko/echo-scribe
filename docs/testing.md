# Testing Strategy

## Unit tests
- **MediaPreparationService** – verifies ffprobe outcomes and conversion branching.
- **TranscriptionService** – ensures the orchestration layer combines acquisition, preparation, and whisper execution.
- **SystemStatusService** – confirms diagnostics aggregation and readiness flags.
- **UrlMediaDownloadService** – validates request errors and download flow behavior.
- **Infrastructure executors** – command-building tests cover ffprobe, ffmpeg, whisper-cli, and the downloader to ensure correct arguments.

All unit tests stub or mock the `ExternalCommandExecutor`, so no real native tools run.

## Integration tests
- **API controllers (MockMvc + @WebMvcTest)** – exercise the JSON endpoints for file uploads, URL submissions, and error handling (ProblemDetail responses).
- **UI controllers (SpringBootTest + MockMvc)** – render the home page and system status page to ensure Thymeleaf wiring works.
- **System status API** – verifies `/api/system/status` returns aggregated readiness.

Integration tests rely on `@MockBean` services so they do not invoke ffmpeg/ffprobe/whisper-cli. This keeps the suite deterministic and runnable in CI without native dependencies.

## External dependencies in tests
Native tools are never executed during automated tests. The process execution layer is abstracted via `ExternalCommandExecutor`, and tests provide fake implementations or mocks that capture command requests and return canned outputs.
