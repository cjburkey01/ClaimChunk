package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdList implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "list";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdList;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[]{new Argument("page", Argument.TabCompletion.NONE)};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        UUID ply = executor.getUniqueId();
        String ownerName = playerHandler.getUsername(executor.getUniqueId());
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        ChunkPos[] chunks = chunkHandler.getClaimedChunks(ply);
        int page = 0;
        final int maxPerPage = Utils.clamp(claimChunk.chConfig().getMaxPerListPage(), 2, 10);
        final int maxPage = Integer.max(0, (chunks.length - 1) / maxPerPage);
        if (args.length == 1) {
            try {
                page = Utils.clamp(Integer.parseInt(args[0]) - 1, 0, maxPage);
            } catch (Exception ignored) {
                Utils.msg(executor, claimChunk.chConfig().getInfoColor() + claimChunk.getMessages().errEnterValidNum);
                return true;
            }
        }

        Utils.msg(executor, String.format("%s&l--- [ %s ] ---", claimChunk.chConfig().getInfoColor(), claimChunk.getMessages().claimsTitle
                .replace("%%NAME%%", ownerName)
                .replace("%%WORLD%%", executor.getWorld().getName())));
        Utils.msg(executor, claimChunk.chConfig().getInfoColor() + claimChunk.getMessages().claimsPagination
                .replace("%%PAGE%%", (page + 1) + "")
                .replace("%%MAXPAGE%%", (maxPage + 1) + ""));
        Utils.msg(executor, "");
        for (int i = page * maxPerPage; (i < (page + 1) * maxPerPage) && (i < chunks.length); i++) {
            // Using `x << 4` is the same as `x * 16` because, in binary,
            // moving digits over one place is equivalent to multiplying by 2.
            // We can multiply by 2 four times (2^4=16). I think bitwise is
            // more efficient than multiplication?
            Utils.msg(executor, claimChunk.chConfig().getInfoColor() + claimChunk.getMessages().claimsChunk
                    .replace("%%X%%", "" + (chunks[i].getX() << 4))
                    .replace("%%Z%%", "" + (chunks[i].getZ() << 4)));
        }
        return true;
    }

}
