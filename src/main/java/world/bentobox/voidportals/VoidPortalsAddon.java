package world.bentobox.voidportals;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.voidportals.listeners.VoidListener;

/**
 * Main add-on class. Registers the void teleport flag with every game mode and
 * starts the listener that teleports players between dimensions when they fall
 * into the void.
 */
public class VoidPortalsAddon extends Addon {

    /**
     * World setting flag. When enabled for a world, a player falling into the void is
     * teleported to the matching location in the next dimension instead of dying.
     */
    public static final Flag VOID_WORLD_TELEPORT_FLAG = new Flag.Builder("VOID_WORLD_TELEPORT_FLAG", Material.ENDER_PEARL)
            .type(Flag.Type.WORLD_SETTING)
            .defaultSetting(false)
            .build();

    @Override
    public void onEnable() {
        // Register the flag with every game mode so it shows up in their admin settings.
        boolean hooked = false;
        for (var gameModeAddon : getPlugin().getAddonsManager().getGameModeAddons()) {
            VOID_WORLD_TELEPORT_FLAG.addGameModeAddon(gameModeAddon);
            hooked = true;
        }

        if (!hooked) {
            logError("VoidPortals could not hook into any game mode so will not do anything!");
            setState(State.DISABLED);
            return;
        }

        registerListener(new VoidListener(this));
        getPlugin().getFlagsManager().registerFlag(VOID_WORLD_TELEPORT_FLAG);
    }

    @Override
    public void onDisable() {
        // Nothing to do here.
    }
}
