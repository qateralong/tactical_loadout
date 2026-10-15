package tactical_loadout.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import tactical_loadout.TacticalLoadout;

@Mod(value = TacticalLoadout.MOD_ID, dist = Dist.CLIENT)
public final class ClientSetup {
    public ClientSetup(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
