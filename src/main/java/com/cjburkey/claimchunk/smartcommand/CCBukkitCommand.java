package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

import lombok.Getter;

import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
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

    public CCBukkitCommand(
            @NotNull String name, @NotNull String[] aliases, @NotNull ClaimChunk claimChunk) {
        super(name);

        this.baseCommand = new ClaimChunkBaseCommand(claimChunk);
        this.claimChunk = claimChunk;

        this.setDescription(
                "The ClaimChunk main command. Use ''/claimchunk help'' or ''/chunk help'' for more"
                        + " information");
        this.setUsage("/<command> help");
        this.setAliases(Arrays.asList(aliases));
        this.register();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender, @NotNull String alias, String[] args) {
        // Is this ok?
        return Objects.requireNonNull(baseCommand.onTabComplete(sender, this, alias, args));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean execute(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return baseCommand.onCommand(sender, this, commandLabel, args);
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
            e.printStackTrace();
        }
    }
}