package de.goldmensch.commanddispatcher;

import de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Commands {

    private Commands() {}

    public static boolean checkExecutor(@NotNull CommandSender sender, @NotNull Executor executor) {
        return executor == Executor.CONSOLE_PLAYER || executor == Executor.fromSender(sender);
    }

    public static boolean checkPermission(
            @NotNull CommandSender sender, @NotNull Optional<String> posPermission) {
        return posPermission.isEmpty() || sender.hasPermission(posPermission.get());
    }

    public static boolean checkPermissionAndExecutor(
            @NotNull CommandSender sender, @NotNull SmartSubCommand command) {
        return Commands.checkExecutor(sender, command.getExecutor())
                && Commands.checkPermission(sender, command.getPermission());
    }
}
