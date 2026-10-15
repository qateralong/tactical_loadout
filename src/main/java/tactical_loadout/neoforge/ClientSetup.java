package tactical_loadout.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import tactical_loadout.registry.ModItems;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import tactical_loadout.TacticalLoadout;

@Mod(value = TacticalLoadout.MOD_ID, dist = Dist.CLIENT)
public final class ClientSetup {
    public ClientSetup(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // The riot shield swaps to its raised model while blocking, like the vanilla shield.
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> ItemProperties.register(ModItems.RIOT_SHIELD.get(),
                TacticalLoadout.location("minecraft", "blocking"), (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F)));
    }
}
