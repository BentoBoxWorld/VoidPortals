package world.bentobox.voidportals.listeners;


import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;


import world.bentobox.bentobox.util.Util;
import world.bentobox.bentobox.util.teleport.SafeSpotTeleport;
import world.bentobox.voidportals.VoidPortalsAddon;


/**
 * Main Void Listener. This class contains listener that detects if player must be
 * teleported via VoidPortal.
 */
public class VoidListener implements Listener
{
	/**
	 * Constructor VoidListener creates a new VoidListener instance.
	 *
	 * @param addon of type VoidPortalsAddon
	 */
	public VoidListener(VoidPortalsAddon addon)
	{
		this.addon = addon;
	}



	/**
	 * Method onPlayerMove disables movement if player is falling in void and alternative
	 * teleport flag is enabled.
	 * It will work only when player reach negative Y coordinates.
	 *
	 * @param event of type PlayerMoveEvent
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeftWorld(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		final double nextY = event.getTo() == null ? 1 : event.getTo().getY();

		if (event.isCancelled() ||
			// ignore dead and spectators
			player.isDead() ||
			player.getGameMode().equals(GameMode.SPECTATOR) ||
			// If above 0, then exit
			nextY > 0 ||
			// Next check will allow to go down, but never up.
			event.getFrom().getY() <= nextY &&
				event.getFrom().getBlockX() == event.getTo().getBlockX() &&
				event.getFrom().getBlockZ() == event.getTo().getBlockZ() ||
			this.addon.getPlayers().isInTeleport(player.getUniqueId()) ||
			// If GameMode addon does not exist, then exit.
			!this.addon.getPlugin().getIWM().getAddon(player.getWorld()).isPresent() ||
			// If world flag is not enabled for current world then exit.
			!VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.isSetForWorld(player.getWorld()))
		{
			return;
		}

		// Use custom teleport to different world
		if (nextY <= 0 && VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.isSetForWorld(player.getWorld()))
		{
			switch (player.getWorld().getEnvironment())
			{
				case NORMAL:
				{
					// From normal world users will get to nether.

					Location to = this.addon.getIslands().getIslandAt(event.getFrom()).
						map(i -> i.getSpawnPoint(World.Environment.NETHER)).
						orElse(event.getFrom().toVector().toLocation(this.addon.getPlugin().getIWM().getAddon(player.getWorld()).get().getNetherWorld()));

					event.setCancelled(true);

					new SafeSpotTeleport.Builder(this.addon.getPlugin()).
						entity(event.getPlayer()).
						location(to).
						portal().
						build();

					break;
				}
				case NETHER:
				{
					// From nether world users will get to the end.

					Location to = this.addon.getIslands().getIslandAt(event.getFrom()).
						map(i -> i.getSpawnPoint(World.Environment.THE_END)).
						orElse(event.getFrom().toVector().toLocation(this.addon.getPlugin().getIWM().getAddon(player.getWorld()).get().getEndWorld()));

					event.setCancelled(true);

					new SafeSpotTeleport.Builder(this.addon.getPlugin()).
						entity(event.getPlayer()).
						location(to).
						portal().
						build();

					break;
				}
				case THE_END:
				{
					// From the end users will get to over world.

					Location to = this.addon.getIslands().getIslandAt(event.getFrom()).
						map(i -> i.getSpawnPoint(World.Environment.NORMAL)).
						orElse(event.getFrom().toVector().toLocation(this.addon.getPlugin().getIWM().getAddon(player.getWorld()).get().getOverWorld()));

					event.setCancelled(true);

					new SafeSpotTeleport.Builder(this.addon.getPlugin()).
						entity(event.getPlayer()).
						location(to).
						portal().
						build();
					break;
				}
				default:
					break;
			}
		}
	}



	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * Main addon class.
	 */
	private VoidPortalsAddon addon;
}
