package commanddispatcher.exceptions;

import claimchunk.dependency.de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;

import org.jetbrains.annotations.NotNull;

public class CommandNotValidException extends RuntimeException {

    @java.io.Serial private static final long serialVersionUID = 17441953375440988L;

    public CommandNotValidException(@NotNull Class<? extends SmartSubCommand> sub) {
        super("Command not valid, class: " + sub.getName());
    }
}
