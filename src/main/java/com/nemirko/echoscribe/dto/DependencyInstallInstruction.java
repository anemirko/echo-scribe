package com.nemirko.echoscribe.dto;

public class DependencyInstallInstruction {

    private final String dependency;
    private final String command;
    private final String notes;

    public DependencyInstallInstruction(String dependency, String command, String notes) {
        this.dependency = dependency;
        this.command = command;
        this.notes = notes;
    }

    public String getDependency() {
        return dependency;
    }

    public String getCommand() {
        return command;
    }

    public String getNotes() {
        return notes;
    }
}
