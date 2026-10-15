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
import tactical_loadout.network.DownedPayload;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.RestraintPayload;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.DownedOverlay;
import tactical_loadout.client.RestraintOverlay;
import tactical_loadout.gear.Restraints;
import tactical_loadout.registry.ModBlocks;
import tactical_loadout.registry.ModItems;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.renderer.RenderType;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModParticles;

public final class TacticalLoadoutFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ClientHooks.TOGGLE_NVG);
        //? if >=1.21 {
        ClientPlayNetworking.registerGlobalReceiver(FlashPayload.TYPE,
                (payload, context) -> context.client().execute(() -> FlashOverlay.trigger(payload.strength(), payload.ticks())));
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(FlashPayload.ID, (client, handler, buf, sender) -> {
            FlashPayload payload = FlashPayload.read(buf);
            client.execute(() -> FlashOverlay.trigger(payload.strength(), payload.ticks()));
        });
        *///?}
        ParticleFactoryRegistry.getInstance().register(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
        EntityRendererRegistry.register(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        //? if >=1.21 {
        ClientPlayNetworking.registerGlobalReceiver(RestraintPayload.TYPE,
                (payload, context) -> context.client().execute(() -> Restraints.setClientRemaining(payload.remaining())));
        ClientPlayNetworking.registerGlobalReceiver(DownedPayload.TYPE,
                (payload, context) -> context.client().execute(() -> ClientHooks.onDowned(payload.entityId(), payload.ticks())));
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(RestraintPayload.ID, (client, handler, buf, sender) -> {
            RestraintPayload payload = RestraintPayload.read(buf);
            client.execute(() -> Restraints.setClientRemaining(payload.remaining()));
        });
        ClientPlayNetworking.registerGlobalReceiver(DownedPayload.ID, (client, handler, buf, sender) -> {
            DownedPayload payload = DownedPayload.read(buf);
            client.execute(() -> ClientHooks.onDowned(payload.entityId(), payload.ticks()));
        });
        *///?}
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TRIPWIRE_ALARM.get(), RenderType.cutout());
        // The riot shield swaps to its raised model while blocking, like the vanilla shield.
        FabricModelPredicateProviderRegistry.register(ModItems.RIOT_SHIELD.get(), TacticalLoadout.location("minecraft", "blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        // The night vision tint is drawn by GuiMixin under the HUD; the flash goes over everything.
        HudRenderCallback.EVENT.register(DownedOverlay::render);
        HudRenderCallback.EVENT.register(RestraintOverlay::render);
        HudRenderCallback.EVENT.register(FlashOverlay::render);
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientHooks.blockHelplessInput());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientHooks.onClientTick());
        ClientTickEvents.END_WORLD_TICK.register(level -> level.players().forEach(ClientHooks::onPlayerTick));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientHooks.onLoggingOut());
    }
}
