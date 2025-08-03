import static org.junit.jupiter.api.Assertions.*;

import com.cjburkey.claimchunk.ClaimChunk;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;
import org.mockbukkit.mockbukkit.world.WorldMock;

public class MockPlayerClaimTest {

    private ServerMock server;
    private ClaimChunk plugin;

    private WorldMock worldOverworld;
    private WorldMock worldNether;

    private PlayerMock chunkOwner;
    private PlayerMock chunkVisitor;

    private Location overworldChunkLocation;
    private Location netherChunkLocation;
    private Chunk overworldChunk;
    private Chunk netherChunk;

    @BeforeEach
    public void setUp() {
        // Start the mock server & load plugin
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(ClaimChunk.class);

        // Fake 1 second's worth of ticks to make sure everything is good to go.
        // Shouldn't matter, but can't hurt, right?
        this.server.getScheduler().performTicks(20);

        // Add two worlds to make sure their claims are separate as expected :shrug:
        this.worldOverworld = this.server.addSimpleWorld("world_overworld");
        this.worldNether = this.server.addSimpleWorld("world_nether");

        // Have two players join
        this.chunkOwner = this.server.addPlayer();
        this.chunkVisitor = this.server.addPlayer();

        // Get some chunks to test claiming for
        double x = -92853.5;
        double z = 2752.5;
        this.overworldChunkLocation = new Location(this.worldOverworld, x, 100.0, z);
        this.netherChunkLocation = new Location(this.worldNether, x, 100.0, z);
        this.overworldChunk = this.overworldChunkLocation.getChunk();
        this.netherChunk = this.netherChunkLocation.getChunk();
    }

    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    // Test: Check to make sure that a player without access to a certain chunk
    //       owned by another player doesn't have, well, access. lol.
    @Test
    public void playerNoPermsVisitShouldDeny() {
        // Make sure both players can damage blocks in the unclaimed chunks.
        this.chunkOwner.setLocation(this.overworldChunkLocation);
        this.chunkVisitor.setLocation(this.overworldChunkLocation);
        assertTrue(canBreakOrDamage(this.chunkOwner, 1, 1, 1));
        assertTrue(canBreakOrDamage(this.chunkVisitor, 1, 1, 1));

        // Chunk owner player claims a chunk in the overworld
        this.chunkOwner.performCommand("chunk claim");
        assertTrue(this.plugin.getChunkHandler().isClaimed(this.overworldChunk));

        // Make sure the owner can still break blocks but the other player is now not allowed
        assertTrue(canBreakOrDamage(this.chunkOwner, 1, 1, 1));
        assertFalse(canBreakOrDamage(this.chunkVisitor, 1, 1, 1));

        // Move visitor to the nether chunk at the same location (same chunk coordinates) to ensure
        // they still have access to chunks in other worlds.
        this.chunkVisitor.setLocation(this.netherChunkLocation);
        assertTrue(canBreakOrDamage(this.chunkVisitor, 1, 1, 1));

        // Move visitor back to claimed overworld chunk
        this.chunkVisitor.setLocation(this.overworldChunkLocation);
        this.chunkOwner.performCommand("chunk unclaim");
        // Visitor should now have access to newly-unclaimed chunk
        assertTrue(canBreakOrDamage(this.chunkVisitor, 1, 1, 1));
    }

    private static boolean canBreakOrDamage(PlayerMock player, int x, int y, int z) {
        // Make sure block is some kind of destructible (should be by default; MockBukkit worlds are
        // superflat by default, and 1,1,1 in a chunk should be in solid minecraft:stone territory.
        var block = player.getChunk().getBlock(x, y, z);
        block.setType(Material.STONE);

        var blockBreakEvent = new PlayerSimulation(player).simulateBlockBreak(block);
        // The event will be null if the player isn't in creative or survival mode, or if the player
        // isn't allowed to damage the block, which should also be the case if the chunk is claimed.
        // Either way, we probably don't need to care.
        if (blockBreakEvent == null) {
            return false;
        }

        // `PlayerSimulation.simulateBlockBreak` sets the block type to air if the event isn't
        // cancelled. Either type of check would work I guess, it might be smarter to test
        // `Event.isCancelled` but idk, I just work here.
        return block.getType() == Material.AIR;
    }
}
