package com.cjburkey.claimchunk.api;

import com.cjburkey.claimchunk.update.SemVer;
import org.bukkit.Server;

public interface IClaimChunkPlugin {

    /**
     * Get an instance of the Bukkit server class.
     *
     * @return Get the server
     */
    Server getServer();

    /**
     * The version for ClaimChunk.
     *
     * @return The version for this installation of ClaimChunk.
     */
    SemVer getVersion();

    /**
     * The latest release of ClaimChunk online.
     *
     * @return The latest GitHub release for ClaimChunk.
     */
    SemVer getAvailableVersion();

}
