package tactical_loadout.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.client.GrenadeRenderer;
import tactical_loadout.client.SmokePuffParticle;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModParticles;

public final class TacticalLoadoutFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ClientHooks.TOGGLE_NVG);
        ClientPlayNetworking.registerGlobalReceiver(FlashPayload.TYPE,
                (payload, context) -> context.client().execute(() -> FlashOverlay.trigger(payload.strength(), payload.ticks())));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
        EntityRendererRegistry.register(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        // The night vision tint is drawn by GuiMixin under the HUD; the flash goes over everything.
        HudRenderCallback.EVENT.register(FlashOverlay::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientHooks.onClientTick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientHooks.onLoggingOut());
    }
}
