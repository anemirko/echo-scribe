package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.media.FfmpegExecutor;
import com.nemirko.echoscribe.infra.media.FfprobeExecutor;
import com.nemirko.echoscribe.infra.media.MediaProbeResult;
import com.nemirko.echoscribe.infra.media.PreparedAudio;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MediaPreparationService {

    private static final Logger log = LoggerFactory.getLogger(MediaPreparationService.class);

    private final TranscriptionProperties properties;
    private final FfprobeExecutor ffprobeExecutor;
    private final FfmpegExecutor ffmpegExecutor;

    public MediaPreparationService(TranscriptionProperties properties,
                                   FfprobeExecutor ffprobeExecutor,
                                   FfmpegExecutor ffmpegExecutor) {
        this.properties = properties;
        this.ffprobeExecutor = ffprobeExecutor;
        this.ffmpegExecutor = ffmpegExecutor;
    }

    public PreparationResult prepare(Path mediaPath) {
        MediaProbeResult probe = ffprobeExecutor.probe(mediaPath);
        log.info("Media probe detected format {}", probe.formatName());
        PreparedAudio audio;
        if (probe.isWavLike()) {
            audio = new PreparedAudio(mediaPath, false);
        } else {
            audio = ffmpegExecutor.convertToWav(mediaPath, properties.tempDirPath());
        }
        return new PreparationResult(probe, audio);
    }

    public static class PreparationResult implements AutoCloseable {
        private final MediaProbeResult probe;
        private final PreparedAudio audio;

        public PreparationResult(MediaProbeResult probe, PreparedAudio audio) {
            this.probe = probe;
            this.audio = audio;
        }

        public MediaProbeResult probe() {
            return probe;
        }

        public PreparedAudio audio() {
            return audio;
        }

        @Override
        public void close() {
            if (audio != null) {
                audio.close();
            }
        }
    }
}
