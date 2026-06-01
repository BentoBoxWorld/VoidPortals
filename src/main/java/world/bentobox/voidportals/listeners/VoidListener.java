package world.bentobox.voidportals.listeners;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.util.teleport.SafeSpotTeleport;
import world.bentobox.voidportals.VoidPortalsAddon;

/**
 * Detects when a player falls into the void in a game mode world and teleports
 * them to the next dimension instead of letting them die. The cycle is
 * Overworld &rarr; Nether &rarr; The End &rarr; Overworld.
 */
public class VoidListener implements Listener {

    private final VoidPortalsAddon addon;

    public VoidListener(VoidPortalsAddon addon) {
        this.addon = addon;
    }

    /**
     * Teleports a player falling into the void to the next dimension. Only fires once
     * the player drops to or below y = 0 in a world where the flag is enabled.
     *
     * @param event the player move event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerFallIntoVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        // Only act once the player has dropped to or below the world floor.
        if (to == null || to.getY() > 0) {
            return;
        }

        // Ignore the dead and spectators.
        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Allow falling, but ignore movement that does not descend within the same column.
        if (from.getY() <= to.getY() || from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            return;
        }

        // Skip players already mid-teleport.
        if (addon.getPlayers().isInTeleport(player.getUniqueId())) {
            return;
        }

        // Only act in a game mode world where the flag is enabled.
        World world = player.getWorld();
        Optional<GameModeAddon> gameMode = addon.getPlugin().getIWM().getAddon(world);
        if (gameMode.isEmpty() || !VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.isSetForWorld(world)) {
            return;
        }

        // Resolve the destination dimension and its world.
        World.Environment target;
        World targetWorld;
        switch (world.getEnvironment()) {
            case NORMAL -> {
                target = World.Environment.NETHER;
                targetWorld = gameMode.get().getNetherWorld();
            }
            case NETHER -> {
                target = World.Environment.THE_END;
                targetWorld = gameMode.get().getEndWorld();
            }
            case THE_END -> {
                target = World.Environment.NORMAL;
                targetWorld = gameMode.get().getOverWorld();
            }
            default -> {
                return;
            }
        }

        if (targetWorld == null) {
            return;
        }

        // Teleport to the island's spawn point in the target dimension, or to the same
        // x/z coordinates there if the player is not on an island.
        Location destination = addon.getIslands().getIslandAt(from)
                .map(island -> island.getSpawnPoint(target))
                .orElse(from.toVector().toLocation(targetWorld));

        event.setCancelled(true);
        new SafeSpotTeleport.Builder(addon.getPlugin())
                .entity(player)
                .location(destination)
                .portal()
                .build();
    }
}
