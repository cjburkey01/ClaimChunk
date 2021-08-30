package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/** @since 0.0.23 */
public class ListCmd extends CCSubCommand {

    public ListCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdList;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public String getPermissionMessage() {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg("page", CCAutoComplete.NONE)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        UUID ply = player.getUniqueId();
        String ownerName = playerHandler.getUsername(player.getUniqueId());
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        ChunkPos[] chunks = chunkHandler.getClaimedChunks(ply);
        int page = 0;
        final int maxPerPage = Utils.clamp(claimChunk.chConfig().getMaxPerListPage(), 2, 10);
        final int maxPage = Integer.max(0, (chunks.length - 1) / maxPerPage);
        if (args.length == 1) {
            try {
                page = Utils.clamp(Integer.parseInt(args[0]) - 1, 0, maxPage);
            } catch (Exception ignored) {
                Utils.msg(
                        player,
                        claimChunk.chConfig().getInfoColor()
                                + claimChunk.getMessages().errEnterValidNum);
                return true;
            }
        }

        Utils.msg(
                player,
                String.format(
                        "%s&l--- [ %s ] ---",
                        claimChunk.chConfig().getInfoColor(),
                        claimChunk
                                .getMessages()
                                .claimsTitle
                                .replace("%%NAME%%", ownerName)
                                .replace("%%WORLD%%", player.getWorld().getName())));
        Utils.msg(
                player,
                claimChunk.chConfig().getInfoColor()
                        + claimChunk
                                .getMessages()
                                .claimsPagination
                                .replace("%%PAGE%%", (page + 1) + "")
                                .replace("%%MAXPAGE%%", (maxPage + 1) + ""));
        Utils.msg(player, "");
        for (int i = page * maxPerPage; (i < (page + 1) * maxPerPage) && (i < chunks.length); i++) {
            // Using `x << 4` is the same as `x * 16` I think bitwise is
            // more efficient than multiplication?
            Utils.msg(
                    player,
                    claimChunk.chConfig().getInfoColor()
                            + claimChunk
                                    .getMessages()
                                    .claimsChunk
                                    .replace("%%X%%", "" + (chunks[i].getX() << 4))
                                    .replace("%%Z%%", "" + (chunks[i].getZ() << 4)));
        }
        return true;
    }
}
