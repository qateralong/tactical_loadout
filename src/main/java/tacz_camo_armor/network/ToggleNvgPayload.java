package tacz_camo_armor.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tacz_camo_armor.TaczCamoArmor;

public record ToggleNvgPayload() implements CustomPacketPayload {
    public static final Type<ToggleNvgPayload> TYPE = new Type<>(TaczCamoArmor.id("toggle_nvg"));
    public static final StreamCodec<ByteBuf, ToggleNvgPayload> STREAM_CODEC = StreamCodec.unit(new ToggleNvgPayload());

    @Override
    public Type<ToggleNvgPayload> type() {
        return TYPE;
    }
}
