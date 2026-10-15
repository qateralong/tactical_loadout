package tactical_loadout.network;

import tactical_loadout.TacticalLoadout;
//? if >=1.20.5 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
*///?}

public record FlashPayload(float strength, int ticks) implements CustomPacketPayload {
    //? if >=1.20.5 {
    public static final Type<FlashPayload> TYPE = new Type<>(TacticalLoadout.id("flash"));
    public static final StreamCodec<ByteBuf, FlashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FlashPayload::strength,
            ByteBufCodecs.VAR_INT, FlashPayload::ticks,
            FlashPayload::new);

    @Override
    public Type<FlashPayload> type() {
        return TYPE;
    }
    //?} else {
    /*public static final ResourceLocation ID = TacticalLoadout.id("flash");

    public static FlashPayload read(FriendlyByteBuf buf) {
        return new FlashPayload(buf.readFloat(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(strength);
        buf.writeVarInt(ticks);
    }
    *///?}
}
