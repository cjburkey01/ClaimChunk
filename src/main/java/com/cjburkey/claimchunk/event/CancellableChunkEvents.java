package com.cjburkey.claimchunk.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;

@SuppressWarnings("unused")
@Deprecated
public class CancellableChunkEvents implements Listener {

    // Fire spreading
    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
    }

    // Fluids spreading
    @EventHandler
    public void onFireSpread(BlockFromToEvent e) {
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e) {
    }

}
