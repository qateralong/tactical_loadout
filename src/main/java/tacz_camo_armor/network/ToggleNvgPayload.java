package tacz_camo_armor.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.Devices;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public record ToggleNvgPayload() implements CustomPacketPayload {
    public static final Type<ToggleNvgPayload> TYPE = new Type<>(TaczCamoArmor.id("toggle_nvg"));
    public static final StreamCodec<ByteBuf, ToggleNvgPayload> STREAM_CODEC = StreamCodec.unit(new ToggleNvgPayload());

    @Override
    public Type<ToggleNvgPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> Devices.toggleNvg(context.player())));
    }
}
