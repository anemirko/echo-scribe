package com.nemirko.echoscribe.infra.process;

public interface ExternalCommandExecutor {

    CommandResult run(CommandRequest request);
}
