package claimchunk.dependency.de.goldmensch.commanddispatcher.command;

import claimchunk.dependency.de.goldmensch.commanddispatcher.ArraySets;
import claimchunk.dependency.de.goldmensch.commanddispatcher.ArrayUtil;
import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;
import claimchunk.dependency.de.goldmensch.commanddispatcher.annotations.Description;
import claimchunk.dependency.de.goldmensch.commanddispatcher.exceptions.CommandNotValidException;
import claimchunk.dependency.de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class SmartCommand implements TabExecutor {
    private final HashMap<String[], SmartSubCommand> subCommandMap = new HashMap<>();

    /***
     * <p>Registers a subcommand, the subcommand may occur only once with the same path!</p>
     * <p>Example: 'registerSubCommand(new AboutSub(ExecutorLevel.CONSOLE_PLAYER, ""), "about");'</p>
     *
     * @param command The subcommand to be executed
     * @param args the complete path of the command including name
     */
    public void registerSubCommand(@NotNull SmartSubCommand command, @NotNull String... args) {
        args = ArrayUtil.toLowerCase(args);
        if (isValid(args)) {
            command.setName(ArrayUtil.buildString(args));
            addAnnotations(command);
            subCommandMap.put(args, command);
        } else {
            throw new CommandNotValidException(command.getClass());
        }
    }

    private void addAnnotations(@NotNull SmartSubCommand command) {
        for (var ann : command.getClass().getAnnotations()) {
            if (ann instanceof Description description) {
                command.setDescription(description.value());
            }
        }
    }

    protected boolean isValid(@NotNull String[] args) {
        return (args.length != 0) && (!subCommandMap.containsKey(args));
    }

    protected @NotNull Optional<SubCommandEntity> searchSub(@NotNull String[] args) {
        var possibleSubCommand = new HashSet<String[]>();
        for (var posArgs : subCommandMap.keySet()) {
            if (ArrayUtil.startWith(args, posArgs)) {
                possibleSubCommand.add(posArgs);
            }
        }

        return !possibleSubCommand.isEmpty()
                ? Optional.of(getBiggest(args, possibleSubCommand))
                : Optional.empty();
    }

    private @NotNull SubCommandEntity getBiggest(
            @NotNull String[] args, @NotNull Set<String[]> possibleCommands) {
        var matchArgs = ArraySets.getBiggest(possibleCommands);
        return new SubCommandEntity(
                subCommandMap.get(matchArgs),
                Arrays.copyOfRange(args, matchArgs.length, args.length));
    }

    /***
     * @hidden
     */
    public abstract boolean noSubFound(
            @NotNull String[] args,
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label);

    /***
     * @hidden
     */
    public abstract void wrongExecutor(
            @NotNull SubCommandEntity command,
            @NotNull CommandSender sender,
            @NotNull Executor requiredExecutor);

    /***
     * @hidden
     */
    public abstract void noPermission(
            @NotNull SubCommandEntity command, @NotNull CommandSender sender);

    /***
     * @hidden
     */
    @Override
    @ApiStatus.Internal
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command bukkitCommand,
            @NotNull String label,
            @NotNull String[] args) {
        var posSubCommand = searchSub(args);
        if (posSubCommand.isEmpty()) {
            return noSubFound(args, sender, bukkitCommand, label);
        }

        var foundCommand = posSubCommand.get();
        var command = foundCommand.getCommand();
        if (!command.rightExecutor(sender)) {
            wrongExecutor(foundCommand, sender, command.getExecutor());
            return true;
        }

        if (!command.rightPermission(sender)) {
            noPermission(foundCommand, sender);
            return true;
        }

        return foundCommand
                .getCommand()
                .onCommand(sender, bukkitCommand, label, foundCommand.getArgs());
    }

    /***
     * @hidden
     */
    @Override
    @ApiStatus.Internal
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] a) {
        var completion = new ArrayList<String>();

        var args = ArrayUtil.toLowerCase(a);
        var argPath = Arrays.copyOf(args, args.length - 1);

        for (var sub : subCommandMap.entrySet()) {
            var argLength = args.length - 1;
            var comArgs = sub.getKey();

            if (!sub.getValue().rightExecutorAndPermission(sender)
                    || (comArgs.length < args.length)) continue;
            var comPath = Arrays.copyOf(comArgs, argLength);
            if (Arrays.equals(comPath, argPath)) {
                var comArg = comArgs[argLength];
                var arg = args[argLength];
                if (comArg.startsWith(arg)) completion.add(comArg);
            }
        }

        var foundCommand = searchSub(args);
        if (foundCommand.isPresent()) {
            var subCommand = foundCommand.get().getCommand();
            if (subCommand.rightExecutorAndPermission(sender)
                    && subCommand instanceof TabCompleter tabCompleter) {
                var commandCompletion =
                        tabCompleter.onTabComplete(
                                sender, command, alias, foundCommand.get().getArgs());
                if (commandCompletion != null) completion.addAll(commandCompletion);
            }
        }

        return completion;
    }

    protected @NotNull Map<String[], SmartSubCommand> getSubCommandMap() {
        return Collections.unmodifiableMap(subCommandMap);
    }

    public static final class SubCommandEntity {
        private final SmartSubCommand command;
        private final String[] args;

        private SubCommandEntity(@NotNull SmartSubCommand command, @NotNull String[] args) {
            this.command = command;
            this.args = args;
        }

        /***
         * <p>The new arguments are the arguments without the path and the name of the command.</p>
         * <p>Example: subcommand path : "arg0 name" </p>
         * <p>path: "arg0 name value" becomes Arguments: "value"</p>
         * @return The new Arguments of the command.
         */
        public @NotNull String[] getArgs() {
            return args;
        }

        /***
         * @return The SubCommand
         */
        public @NotNull SmartSubCommand getCommand() {
            return command;
        }
    }
}
