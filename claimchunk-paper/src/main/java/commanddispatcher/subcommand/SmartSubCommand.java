package commanddispatcher.subcommand;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import lombok.Getter;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class SmartSubCommand implements CommandExecutor {

    private final Executor executor;
    @Getter private final String[] permissions;
    private String name;
    @Getter private String description;

    // vararg
    public SmartSubCommand(@NotNull Executor executor, String... permissions) {
        this.executor = executor;
        this.permissions = permissions;
    }

    @ApiStatus.Internal
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @ApiStatus.Internal
    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    /***
     * @return The {@link Executor} of the SubCommand
     */
    public @NotNull Executor getExecutor() {
        return executor;
    }

    /***
     * Checks if the provided player should be able to execute a given command.
     *
     * @param sender The player whose permission we need to check.
     * @return Whether the player should be allowed to execute this command.
     */
    public boolean rightPermission(@NotNull CommandSender sender) {
        return permissions.length == 0
                || Arrays.stream(permissions).anyMatch(sender::hasPermission);
    }

    /***
     * Checks if the provided command executor should be able to execute a given command.
     *
     * @param sender The sender whose executor type.
     * @return Whether the executor should be allowed to execute this command.
     */
    public boolean rightExecutor(@NotNull CommandSender sender) {
        Executor senderExecutor = Executor.fromSender(sender);
        return getExecutor() == Executor.CONSOLE_PLAYER || senderExecutor == getExecutor();
    }

    /***
     * Checks if the provided sender should be able to execute a given command. If the console is
     * the executor, {@code true} is always returned.
     *
     * @param sender The sender whose permission we need to check.
     * @return Whether the given sender should be allowed to execute this command based on type and permission.
     */
    public boolean rightExecutorAndPermission(@NotNull CommandSender sender) {
        Executor senderExecutor = Executor.fromSender(sender);
        return rightExecutor(sender)
                && (senderExecutor == Executor.CONSOLE || rightPermission(sender));
    }

    /***
     * <p>The name is a string consisting of the path and the name. </p>
     * <p>Example: The StringArray {"arg0", "arg1", "name"} becomes "arg0 arg1 name"</p>
     * @return The name of the SubCommand
     */
    public @NotNull String getName() {
        return name;
    }
}
