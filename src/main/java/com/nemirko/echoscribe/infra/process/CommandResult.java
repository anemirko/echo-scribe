package com.nemirko.echoscribe.infra.process;

public class CommandResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public CommandResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int exitCode() {
        return exitCode;
    }

    public String stdout() {
        return stdout;
    }

    public String stderr() {
        return stderr;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }
}
