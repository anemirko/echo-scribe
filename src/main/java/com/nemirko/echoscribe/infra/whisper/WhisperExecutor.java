package com.nemirko.echoscribe.infra.whisper;

import java.nio.file.Path;
import java.util.Optional;

public interface WhisperExecutor {

    WhisperResult transcribe(Path audioFile, Optional<String> language);
}
