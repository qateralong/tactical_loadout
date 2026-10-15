package tacz_camo_armor.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.client.ClientHooks;
import tacz_camo_armor.client.FlashOverlay;
import tacz_camo_armor.client.GrenadeRenderer;
import tacz_camo_armor.client.NvgOverlay;
import tacz_camo_armor.client.SmokePuffParticle;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModParticles;

/** Forwards NeoForge client events to the shared client hooks. */
@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        ClientHooks.TOGGLE_NVG.setKeyConflictContext(KeyConflictContext.IN_GAME);
        event.register(ClientHooks.TOGGLE_NVG);
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, TaczCamoArmor.id("nvg"), NvgOverlay::render);
        event.registerAboveAll(TaczCamoArmor.id("flash"), FlashOverlay::render);
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
    }

    @SubscribeEvent
    static void onFov(ComputeFovModifierEvent event) {
        event.setNewFovModifier(ClientHooks.fovModifier(event.getPlayer(), event.getNewFovModifier()));
    }

    @SubscribeEvent
    static void onMovementInput(MovementInputUpdateEvent event) {
        ClientHooks.onMovementInput(event.getEntity(), event.getInput());
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHooks.onLoggingOut();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ClientHooks.onClientTick();
    }
}
