package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.media.FfmpegExecutor;
import com.nemirko.echoscribe.infra.media.FfprobeExecutor;
import com.nemirko.echoscribe.infra.media.MediaProbeResult;
import com.nemirko.echoscribe.infra.media.PreparedAudio;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MediaPreparationServiceTest {

    private final TranscriptionProperties properties = new TranscriptionProperties();
    private final FfprobeExecutor ffprobeExecutor = mock(FfprobeExecutor.class);
    private final FfmpegExecutor ffmpegExecutor = mock(FfmpegExecutor.class);
    private final MediaPreparationService service = new MediaPreparationService(properties, ffprobeExecutor, ffmpegExecutor);

    @Test
    void usesOriginalWhenAlreadyWav() {
        Path media = Path.of("sample.wav");
        when(ffprobeExecutor.probe(media)).thenReturn(new MediaProbeResult("wav", null));

        try (var result = service.prepare(media)) {
            assertThat(result.audio().audioFile()).isEqualTo(media);
            verify(ffmpegExecutor, times(0)).convertToWav(media, properties.tempDirPath());
        }
    }

    @Test
    void convertsWhenNotWav() {
        Path media = Path.of("video.mp4");
        PreparedAudio prepared = new PreparedAudio(Path.of("converted.wav"), true);
        when(ffprobeExecutor.probe(media)).thenReturn(new MediaProbeResult("mp4", null));
        when(ffmpegExecutor.convertToWav(media, properties.tempDirPath())).thenReturn(prepared);

        try (var result = service.prepare(media)) {
            assertThat(result.audio().audioFile()).isEqualTo(prepared.audioFile());
        }
    }
}
