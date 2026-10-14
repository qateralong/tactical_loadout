package tacz_camo_armor.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.client.FlashOverlay;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public record FlashPayload(float strength, int ticks) implements CustomPacketPayload {
    public static final Type<FlashPayload> TYPE = new Type<>(TaczCamoArmor.id("flash"));
    public static final StreamCodec<ByteBuf, FlashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FlashPayload::strength,
            ByteBufCodecs.VAR_INT, FlashPayload::ticks,
            FlashPayload::new);

    @Override
    public Type<FlashPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> FlashOverlay.trigger(payload.strength(), payload.ticks())));
    }
}
