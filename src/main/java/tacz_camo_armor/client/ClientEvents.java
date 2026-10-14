package tacz_camo_armor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.network.ToggleNvgPayload;
import tacz_camo_armor.registry.ModEntities;

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
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GRENADE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ThermalVision.updateShader();
        while (TOGGLE_NVG.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                PacketDistributor.sendToServer(new ToggleNvgPayload());
            }
        }
    }
}
