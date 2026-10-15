package tacz_camo_armor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ClientConfig;
import tacz_camo_armor.grenade.GrenadeItem;
import tacz_camo_armor.network.ToggleNvgPayload;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModParticles;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    public static final KeyMapping TOGGLE_NVG = new KeyMapping("key." + TaczCamoArmor.MOD_ID + ".nvg",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories." + TaczCamoArmor.MOD_ID);

    private ClientEvents() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_NVG);
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
        float charge = grenadeCharge(event.getPlayer());
        if (charge > 0.0F && ClientConfig.throwFovEffect()) {
            double scale = Minecraft.getInstance().options.fovEffectScale().get();
            event.setNewFovModifier(event.getNewFovModifier() * (1.0F + 0.12F * charge * (float) scale));
        }
    }

    // Winding up a throw should not crawl like drawing a bow; vanilla multiplies the input by 0.2 right after this event.
    @SubscribeEvent
    static void onMovementInput(MovementInputUpdateEvent event) {
        if (grenadeCharge(event.getEntity()) > 0.0F) {
            event.getInput().forwardImpulse *= 3.5F;
            event.getInput().leftImpulse *= 3.5F;
        }
    }

    private static float grenadeCharge(Player player) {
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof GrenadeItem)) {
            return 0.0F;
        }
        return Math.max(0.01F, GrenadeItem.charge(player.getTicksUsingItem()));
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FlashOverlay.reset();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ThermalVision.tick();
        while (TOGGLE_NVG.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                PacketDistributor.sendToServer(new ToggleNvgPayload());
            }
        }
    }
}
