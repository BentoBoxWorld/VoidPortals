package world.bentobox.voidportals;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class VoidPortalsPladdon extends Pladdon {

    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new VoidPortalsAddon();
        }
        return addon;
    }
}
