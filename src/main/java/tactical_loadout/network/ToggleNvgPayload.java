package tactical_loadout.network;

import tactical_loadout.TacticalLoadout;
//? if >=1.20.5 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
*///?}

public record ToggleNvgPayload() implements CustomPacketPayload {
    //? if >=1.20.5 {
    public static final Type<ToggleNvgPayload> TYPE = new Type<>(TacticalLoadout.id("toggle_nvg"));
    public static final StreamCodec<ByteBuf, ToggleNvgPayload> STREAM_CODEC = StreamCodec.unit(new ToggleNvgPayload());

    @Override
    public Type<ToggleNvgPayload> type() {
        return TYPE;
    }
    //?} else {
    /*public static final ResourceLocation ID = TacticalLoadout.id("toggle_nvg");

    public static ToggleNvgPayload read(FriendlyByteBuf buf) {
        return new ToggleNvgPayload();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
    *///?}
}
