package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkOutlineHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** @since 0.0.23 */
public class InfoCmd extends CCSubCommand {
    public InfoCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, "info", true);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdInfo);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var playerHandler = claimChunk.getPlayerHandler();
        var chunk = player.getLocation().getChunk();
        var chunkPos = new ChunkPos(chunk);
        var owner = claimChunk.getChunkHandler().getOwner(chunk);

        var ownerName = ((owner == null) ? null : playerHandler.getUsername(owner));
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        var ownerDisplay =
                ((owner == null || !playerHandler.hasChunkName(owner))
                        ? null
                        : playerHandler.getChunkName(owner));
        if (ownerDisplay == null) ownerDisplay = claimChunk.getMessages().infoNameNone;

        messageChat(
                player,
                String.format(
                        claimChunk.getMessages().infoHeader, // "%s&l--- [ %s ] ---",
                        claimChunk.getConfigHandler().getInfoColor(),
                        claimChunk.getMessages().infoTitle));
        messageChat(
                player,
                claimChunk.getConfigHandler().getInfoColor()
                        + (claimChunk
                                .getMessages()
                                .infoPosition
                                .replace("%%X%%", "" + chunk.getX())
                                .replace("%%Z%%", "" + chunk.getZ())
                                .replace("%%WORLD%%", chunk.getWorld().getName())));

        if(ownerName.equals(player.getName())) {
            ownerName += " (you)"; //TODO: add to messages config
        }

        messageChat(
                player,
                claimChunk.getConfigHandler().getInfoColor()
                        + claimChunk.getMessages().infoOwner.replace("%%PLAYER%%", ownerName));
        messageChat(
                player,
                claimChunk.getConfigHandler().getInfoColor()
                        + claimChunk.getMessages().infoName.replace("%%NAME%%", ownerDisplay));
        Particle particle;
        particle = Particle.SMOKE_NORMAL;

        if(owner == null) {
            TextComponent cCommand = new TextComponent(ChatColor.YELLOW + "/chunk claim");
            cCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chunk claim"));
            cCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(claimChunk.getMessages().claimNow)));

            TextComponent message = new TextComponent(claimChunk.getConfigHandler().getInfoColor() + claimChunk.getMessages().toClaimUse + " ");
            message.addExtra(cCommand);
            message.addExtra(claimChunk.getConfigHandler().getInfoColor() + " " + claimChunk.getMessages().toClaimUseCommand);


            messageChatComponent(
                    player,
                    message); //Todo: single message with placeholders for clickable command?

            try {
                particle = Particle.valueOf(claimChunk
                        .getConfigHandler().getChunkFreeOutlineParticle());
            } catch (Exception ex) {
                particle = Particle.SMOKE_NORMAL;
            }
        }

        ChunkOutlineHandler chunkOutlineHandler = //Todo: showChunkFor method with particle argument to avoid create separate ChunkOutlineHandler or create some decorator/proxy
                new ChunkOutlineHandler(
                        claimChunk,
                        particle,
                        20 / claimChunk
                                .getConfigHandler().getChunkOutlineSpawnPerSec(),
                        claimChunk
                                .getConfigHandler().getChunkOutlineHeightRadius(),
                        claimChunk
                                .getConfigHandler().getChunkOutlineParticlesPerSpawn());

        chunkOutlineHandler
                .showChunkFor(
                        chunkPos,
                        player,
                        claimChunk
                                .getConfigHandler()
                                .getChunkOutlineDurationSeconds(),
                        ChunkOutlineHandler.OutlineSides.makeAll(true));

        return true;
    }
}
