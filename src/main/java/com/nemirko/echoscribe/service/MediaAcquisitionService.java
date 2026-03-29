package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.download.DownloadedMedia;
import com.nemirko.echoscribe.model.AcquiredMedia;
import com.nemirko.echoscribe.model.TranscriptionSourceType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaAcquisitionService {

    private static final Logger log = LoggerFactory.getLogger(MediaAcquisitionService.class);

    private final TranscriptionProperties properties;
    private final UrlMediaDownloadService urlMediaDownloadService;

    public MediaAcquisitionService(TranscriptionProperties properties,
                                   UrlMediaDownloadService urlMediaDownloadService) {
        this.properties = properties;
        this.urlMediaDownloadService = urlMediaDownloadService;
    }

    public AcquiredMedia acquireFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("A media file must be provided");
        }
        String filename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "upload";
        try {
            Path tempDir = Files.createDirectories(properties.tempDirPath());
            Path storedFile = Files.createTempFile(tempDir, "upload-", "-" + filename.replaceAll("\\s+", "_"));
            file.transferTo(storedFile);
            log.info("Stored uploaded file {} at {}", filename, storedFile);
            AcquiredMedia media = new AcquiredMedia(
                    TranscriptionSourceType.FILE_UPLOAD,
                    storedFile,
                    filename,
                    null);
            return media;
        } catch (IOException e) {
            throw new InvalidRequestException("Unable to store uploaded file", e);
        }
    }

    public AcquiredMedia acquireFromUrl(String url) {
        DownloadedMedia downloaded = urlMediaDownloadService.download(url);
        AcquiredMedia media = new AcquiredMedia(
                TranscriptionSourceType.URL_DOWNLOAD,
                downloaded.mediaFile(),
                downloaded.mediaFile().getFileName().toString(),
                url);
        media.registerCleanup(downloaded.workingDirectory());
        return media;
    }

    public void validateUrl(String url) {
        urlMediaDownloadService.validateUrl(url);
    }
}
