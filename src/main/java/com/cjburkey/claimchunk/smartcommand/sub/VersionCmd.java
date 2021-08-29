package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.update.SemVer;
import de.goldmensch.commanddispatcher.ExecutorLevel;
import de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class VersionCmd extends SmartSubCommand {

    private final ClaimChunk claimChunk;

    public VersionCmd(ClaimChunk claimChunk) {
        super(ExecutorLevel.CONSOLE_PLAYER, "");

        this.claimChunk = claimChunk;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        SemVer currentVersion = claimChunk.getVersion();
        SemVer newVersion = claimChunk.getAvailableVersion();

        Utils.msg(sender, "&aClaimChunk current version: &e" + currentVersion);
        Utils.msg(sender, "&aClaimChunk available version: &e"
                + (newVersion == null ? "Unknown" : newVersion.toString()));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        return null;
    }

}
