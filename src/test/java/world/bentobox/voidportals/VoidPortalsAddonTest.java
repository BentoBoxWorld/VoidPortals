package world.bentobox.voidportals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.voidportals.listeners.VoidListener;

/**
 * Tests for {@link VoidPortalsAddon}.
 */
class VoidPortalsAddonTest extends CommonTestSetup {

    @Mock
    private AddonsManager am;

    private VoidPortalsAddon addon;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(plugin.getAddonsManager()).thenReturn(am);
        when(plugin.getFlagsManager()).thenReturn(fm);
        addon = new VoidPortalsAddon();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testOnEnableNoGameModeDisablesAddon() {
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());

        addon.onEnable();

        assertEquals(State.DISABLED, addon.getState());
        verify(fm, never()).registerFlag(any());
        verify(am, never()).registerListener(any(), any());
    }

    @Test
    void testOnEnableHooksIntoGameMode() {
        GameModeAddon gameMode = mock(GameModeAddon.class);
        when(am.getGameModeAddons()).thenReturn(List.of(gameMode));

        addon.onEnable();

        verify(fm).registerFlag(VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG);
        verify(am).registerListener(eq(addon), any(VoidListener.class));
    }

    @Test
    void testOnDisableDoesNotThrow() {
        addon.onDisable();
        assertNotNull(addon);
    }

    @Test
    void testFlagIsWorldSetting() {
        assertEquals(Flag.Type.WORLD_SETTING,
                VoidPortalsAddon.VOID_WORLD_TELEPORT_FLAG.getType());
    }
}
