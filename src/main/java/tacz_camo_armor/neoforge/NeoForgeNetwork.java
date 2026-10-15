package tacz_camo_armor.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.client.FlashOverlay;
import tacz_camo_armor.device.Devices;
import tacz_camo_armor.network.FlashPayload;
import tacz_camo_armor.network.ToggleNvgPayload;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class NeoForgeNetwork {
    private NeoForgeNetwork() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(FlashPayload.TYPE, FlashPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> FlashOverlay.trigger(payload.strength(), payload.ticks())))
                .playToServer(ToggleNvgPayload.TYPE, ToggleNvgPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> Devices.toggleNvg(context.player())));
    }
}
