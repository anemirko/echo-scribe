package com.nemirko.echoscribe;

import com.nemirko.echoscribe.config.LoggingProperties;
import com.nemirko.echoscribe.config.TranscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TranscriptionProperties.class, LoggingProperties.class})
public class EchoScribeApplication {

    private static final Logger log = LoggerFactory.getLogger(EchoScribeApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(EchoScribeApplication.class, args);
        log.info("EchoScribe application started. Active profiles: {}", (Object) context.getEnvironment().getActiveProfiles());
    }
}
