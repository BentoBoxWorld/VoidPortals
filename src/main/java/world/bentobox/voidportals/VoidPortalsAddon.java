package world.bentobox.voidportals;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.level.Level;
import world.bentobox.voidportals.listeners.VoidListener;


/**
 * Main addon Class. It starts all processes so addon could properly work.
 */
public class VoidPortalsAddon extends Addon
{
	/**
	 * Executes code when loading the addon. This is called before {@link #onEnable()}. This <b>must</b> be
	 * used to setup configuration, worlds and commands.
	 */
	@Override
	public void onLoad()
	{
		super.onLoad();
	}


	/**
	 * Executes code when enabling the addon. This is called after {@link #onLoad()}. <br/> Note that commands
	 * and worlds registration <b>must</b> be done in {@link #onLoad()}, if need be. Failure to do so
	 * <b>will</b> result in issues such as tab-completion not working for commands.
	 */
	@Override
	public void onEnable()
	{
		// Check if it is enabled - it might be loaded, but not enabled.
		if (this.getPlugin() == null || !this.getPlugin().isEnabled())
		{
			Bukkit.getLogger().severe("BentoBox is not available or disabled!");
			this.setState(State.DISABLED);
			return;
		}

		// Check if addon is not disabled before.
		if (this.getState().equals(State.DISABLED))
		{
			Bukkit.getLogger().severe("VoidPortals Addon is not available or disabled!");
			return;
		}

		// Register game mode in world flag.
		this.getPlugin().getAddonsManager().getGameModeAddons().forEach(
			gameModeAddon -> {
				VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.addGameModeAddon(gameModeAddon);
				this.hooked = true;
			});

		if (this.hooked)
		{
			// Try to find Level addon and if it does not exist, display a warning

			Optional<Addon> level = this.getAddonByName("Level");

			if (!level.isPresent())
			{
				this.logWarning("Level add-on not found so VoidPortals will not work correctly!");
				this.levelAddon = null;
			}
			else
			{
				this.levelProvided = true;
				this.levelAddon = (Level) level.get();
			}

			Optional<VaultHook> vault = this.getPlugin().getVault();

			if (!vault.isPresent() || !vault.get().hook())
			{
				this.vaultHook = null;
				this.logWarning("Economy plugin not found so money options will not work!");
			}
			else
			{
				this.economyProvided = true;
				this.vaultHook = vault.get();
			}


			// Register the listener.
			this.registerListener(new VoidListener(this));

			// Register Flags
			this.getPlugin().getFlagsManager().registerFlag(VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG);

			// Register Request Handlers
//			this.registerRequestHandler(REQUEST_HANDLER);
		}
		else
		{
			this.logError("VoidPortals could not hook into any GameMode so will not do anything!");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * Executes code when disabling the addon.
	 */
	@Override
	public void onDisable()
	{
		// Do some staff...
	}


	/**
	 * Executes code when reloading the addon.
	 */
	@Override
	public void onReload()
	{
		super.onReload();
	}


	// ---------------------------------------------------------------------
	// Section: Getters
	// ---------------------------------------------------------------------


	/**
	 * This method returns the levelAddon object.
	 * @return the levelAddon object.
	 */
	public Level getLevelAddon()
	{
		return this.levelAddon;
	}


	/**
	 * This method returns the levelProvided object.
	 * @return the levelProvided object.
	 */
	public boolean isLevelProvided()
	{
		return this.levelProvided;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * Variable indicates if addon is hooked in any game mode
	 */
	private boolean hooked;

	/**
	 * This boolean indicate if economy is enabled.
	 */
	private boolean economyProvided;

	/**
	 * VaultHook that process economy.
	 */
	private VaultHook vaultHook;

	/**
	 * Level addon.
	 */
	private Level levelAddon;

	/**
	 * This indicate if level addon exists.
	 */
	private boolean levelProvided;


	// ---------------------------------------------------------------------
	// Section: Constants
	// ---------------------------------------------------------------------


	/**
	 * This flag allows enables and disables void world teleport paths. If player falls
	 * into void and this flag is enabled, then he will be teleported to different world.
	 */
	public final static Flag VOID_WORLD_TELEPORT_FLAG =
		new Flag.Builder("VOID_WORLD_TELEPORT_FLAG", Material.ENDER_PEARL).
			type(Flag.Type.WORLD_SETTING).
			defaultSetting(false).
			build();
}
