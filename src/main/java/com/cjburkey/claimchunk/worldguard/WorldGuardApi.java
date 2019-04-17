package com.cjburkey.claimchunk.worldguard;

import com.cjburkey.claimchunk.Utils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;

/**
 * Created by CJ Burkey on 2019/04/16
 *
 * <p>
 * THIS CLASS IS A RAW IMPLEMENTATION
 * IT WILL CRASH IF WORLD GUARD IS NOT PRESENT
 * USE {@link com.cjburkey.claimchunk.worldguard.WorldGuardHandler} instead
 * </p>
 */
class WorldGuardApi {

    private static final String CHUNK_CLAIM_FLAG_NAME = "chunk-claim";
    private static final StateFlag FLAG_CHUNK_CLAIM = new StateFlag(CHUNK_CLAIM_FLAG_NAME, true);

    static boolean _init() {
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            registry.register(FLAG_CHUNK_CLAIM);
            return true;
        } catch (FlagConflictException ignored) {
            Utils.err("Flag \"{}\" is already registered with WorldGuard. Another plugin conflicts with us!", CHUNK_CLAIM_FLAG_NAME);
        } catch (Exception e) {
            Utils.err("Failed to initialize WorldGuard support");
            e.printStackTrace();
        }
        return false;
    }

    static boolean _isAllowedClaim(Chunk chunk) {
        try {
            // Generate a region in the given chunk to get all intersecting regions
            int bx = chunk.getX() << 4;
            int bz = chunk.getZ() << 4;
            BlockVector3 pt1 = BlockVector3.at(bx, 0, bz);
            BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
            ProtectedCuboidRegion region = new ProtectedCuboidRegion("_", pt1, pt2);
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(chunk.getWorld()));

            // TODO: WHETHER CLAIMING IS ALLOWED IN A NON-WORLDGUARD WORLD SHOULD DEPEND ON CONFIG OPTION!
            // No regions in this world, claiming should be (TODO: CONFIG)
            if (regionManager == null) return true;

            // If any regions in the given chunk deny chunk claiming, false is returned
            for (ProtectedRegion regionIn : regionManager.getApplicableRegions(region)) {
                StateFlag.State flag = regionIn.getFlag(FLAG_CHUNK_CLAIM);
                if (flag == StateFlag.State.DENY) return false;
            }

            // No objections
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // An error occurred, better to be on the safe side so false is returned
        return false;
    }

}
