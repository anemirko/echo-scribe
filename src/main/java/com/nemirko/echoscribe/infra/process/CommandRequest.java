package com.nemirko.echoscribe.infra.process;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandRequest {

    private final List<String> command;
    private final Duration timeout;
    private final Path workingDirectory;
    private final Map<String, String> environment;

    private CommandRequest(Builder builder) {
        this.command = List.copyOf(builder.command);
        this.timeout = builder.timeout;
        this.workingDirectory = builder.workingDirectory;
        this.environment = Collections.unmodifiableMap(builder.environment);
    }

    public List<String> command() {
        return command;
    }

    public Duration timeout() {
        return timeout;
    }

    public Path workingDirectory() {
        return workingDirectory;
    }

    public Map<String, String> environment() {
        return environment;
    }

    public static Builder builder(List<String> command) {
        Objects.requireNonNull(command, "command");
        return new Builder(command);
    }

    public static class Builder {
        private final List<String> command;
        private Duration timeout = Duration.ofMinutes(1);
        private Path workingDirectory;
        private Map<String, String> environment = Map.of();

        private Builder(List<String> command) {
            this.command = command;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder workingDirectory(Path workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder environment(Map<String, String> environment) {
            this.environment = environment == null ? Map.of() : environment;
            return this;
        }

        public CommandRequest build() {
            return new CommandRequest(this);
        }
    }
}
