package world.bentobox.voidportals.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.voidportals.CommonTestSetup;
import world.bentobox.voidportals.TestWorldSettings;
import world.bentobox.voidportals.VoidPortalsAddon;

/**
 * Tests for {@link VoidListener}.
 */
class VoidListenerTest extends CommonTestSetup {

    @Mock
    private VoidPortalsAddon addon;
    @Mock
    private PlayersManager pm;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private World netherWorld;
    @Mock
    private World endWorld;
    @Mock
    private World overWorld;

    private VoidListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getIslands()).thenReturn(im);

        // Player is not mid-teleport by default.
        when(pm.isInTeleport(any())).thenReturn(false);

        // The player is in a game mode world.
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameMode));
        when(gameMode.getNetherWorld()).thenReturn(netherWorld);
        when(gameMode.getEndWorld()).thenReturn(endWorld);
        when(gameMode.getOverWorld()).thenReturn(overWorld);

        // No island at the fall location by default — teleport uses raw coordinates.
        when(im.getIslandAt(any())).thenReturn(Optional.empty());

        // Enable the flag for the world.
        TestWorldSettings settings = new TestWorldSettings();
        settings.getWorldFlags().put(VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.getID(), true);
        when(iwm.getWorldSettings(any())).thenReturn(settings);

        // Keep SafeSpotTeleport from doing real async chunk work.
        mockedUtil.when(() -> Util.getChunkAtAsync(any(Location.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(Chunk.class)));

        listener = new VoidListener(addon);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /** A location mock at the given height in the column x=0, z=0. */
    private Location loc(double y) {
        Location l = mock(Location.class);
        when(l.getY()).thenReturn(y);
        when(l.getBlockX()).thenReturn(0);
        when(l.getBlockZ()).thenReturn(0);
        when(l.getWorld()).thenReturn(world);
        when(l.toVector()).thenReturn(new Vector(0, y, 0));
        return l;
    }

    /** A move event descending from y=64 to y=-5 in the same column (a void fall). */
    private PlayerMoveEvent descendingIntoVoid() {
        return new PlayerMoveEvent(mockPlayer, loc(64), loc(-5));
    }

    // ── Dimension routing (happy paths) ─────────────────────────────────────

    @Test
    void testOverworldFallTeleportsToNether() {
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertTrue(event.isCancelled());
        verify(gameMode).getNetherWorld();
    }

    @Test
    void testNetherFallTeleportsToEnd() {
        when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertTrue(event.isCancelled());
        verify(gameMode).getEndWorld();
    }

    @Test
    void testEndFallTeleportsToOverworld() {
        when(world.getEnvironment()).thenReturn(World.Environment.THE_END);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertTrue(event.isCancelled());
        verify(gameMode).getOverWorld();
    }

    @Test
    void testTeleportsToIslandSpawnPointWhenOnIsland() {
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        Island island = mock(Island.class);
        Location spawn = loc(40);
        when(island.getSpawnPoint(World.Environment.NETHER)).thenReturn(spawn);
        when(im.getIslandAt(any())).thenReturn(Optional.of(island));

        PlayerMoveEvent event = descendingIntoVoid();
        listener.onPlayerFallIntoVoid(event);

        assertTrue(event.isCancelled());
        verify(island).getSpawnPoint(World.Environment.NETHER);
    }

    // ── Guard clauses ───────────────────────────────────────────────────────

    @Test
    void testAboveVoidDoesNothing() {
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        // Falling but still well above the floor (y > 0).
        PlayerMoveEvent event = new PlayerMoveEvent(mockPlayer, loc(70), loc(40));

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
        verify(gameMode, never()).getNetherWorld();
    }

    @Test
    void testDeadPlayerIgnored() {
        when(mockPlayer.isDead()).thenReturn(true);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void testSpectatorIgnored() {
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void testHorizontalMovementBelowFloorIgnored() {
        // Same column, no descent (from.y <= to.y) — not a fall.
        PlayerMoveEvent event = new PlayerMoveEvent(mockPlayer, loc(-2), loc(-2));

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void testAlreadyTeleportingIgnored() {
        when(pm.isInTeleport(any())).thenReturn(true);
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void testNotInGameModeWorldIgnored() {
        when(iwm.getAddon(world)).thenReturn(Optional.empty());
        PlayerMoveEvent event = descendingIntoVoid();

        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void testFlagDisabledIgnored() {
        TestWorldSettings settings = new TestWorldSettings();
        settings.getWorldFlags().put(VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.getID(), false);
        when(iwm.getWorldSettings(any())).thenReturn(settings);

        PlayerMoveEvent event = descendingIntoVoid();
        listener.onPlayerFallIntoVoid(event);

        assertFalse(event.isCancelled());
    }
}
