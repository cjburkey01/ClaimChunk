package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

import lombok.Getter;

import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Borrowed from <a
 * href="https://github.com/Goldmensch/ClaimChunk/blob/b9c3e5de965a0466f124195a7147b88a438009c2/src/main/java/com/cjburkey/claimchunk/CCCommand.java">Goldmensch</a>.
 *
 * @since 0.0.23
 */
public class CCBukkitCommand extends BukkitCommand {

    @Getter private final ClaimChunkBaseCommand baseCommand;

    private final ClaimChunk claimChunk;
    private final String[] aliases;

    public CCBukkitCommand(
            @NotNull String name, @NotNull String[] aliases, @NotNull ClaimChunk claimChunk) {
        super(name);

        this.aliases = aliases;
        this.baseCommand = new ClaimChunkBaseCommand(claimChunk);
        this.claimChunk = claimChunk;
    }

    public void registerCommand() {
        this.setDescription(
                "The ClaimChunk main command. Use ''/claimchunk help'' or ''/chunk help'' for more"
                        + " information");
        this.setUsage("/<command> help");
        this.setAliases(Arrays.asList(aliases));
        this.register();
    }

    @Override
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender, @NotNull String alias, String[] args) {
        return baseCommand.onTabComplete(sender, this, alias, args);
    }

    @Override
    public boolean execute(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return baseCommand.onCommand(sender, this, commandLabel, args);
    }

    private static Object getPrivateField(Object object, String field)
            throws SecurityException,
                    NoSuchFieldException,
                    IllegalArgumentException,
                    IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField =
                field.equals("commandMap")
                        ? clazz.getDeclaredField(field)
                        : field.equals("knownCommands")
                                ? clazz.getSuperclass().getDeclaredField(field)
                                : null;
        Objects.requireNonNull(objectField).setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public void removeFromMap() {
        try {
            Object result =
                    getPrivateField(claimChunk.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(this.getName());
            for (String alias : this.getAliases()) {
                if (knownCommands.containsKey(alias)
                        && knownCommands.get(alias).toString().contains(claimChunk.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (Exception e) {
            Utils.err(
                    "Failed to unregister command! If you are reloading, updates to permissions"
                            + " won't appear until a server reboot.");
            if (Utils.getDebugEnableOverride()) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    private void register() {
        try {
            final Field bukkitCommandMap =
                    claimChunk.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(claimChunk.getServer());

            if (!commandMap.register(getName(), this)) {
                Utils.err("Failed to register the main ClaimChunk command!!");
                Utils.err(
                        "Unless you're reloading ClaimChunk via `/chunk admin reload`, there may be"
                                + " an error!");
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Utils.err("ERROR FINDING BUKKIT COMMAND MAP USING REFLECTION!!");
            Utils.err("This is a slightly very big problem!!");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
