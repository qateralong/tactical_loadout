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

/** One press of the struggle key from a tied player. */
public record StrugglePayload() implements CustomPacketPayload {
    //? if >=1.20.5 {
    public static final Type<StrugglePayload> TYPE = new Type<>(TacticalLoadout.id("struggle"));
    public static final StreamCodec<ByteBuf, StrugglePayload> STREAM_CODEC = StreamCodec.unit(new StrugglePayload());

    @Override
    public Type<StrugglePayload> type() {
        return TYPE;
    }
    //?} else {
    /*public static final ResourceLocation ID = TacticalLoadout.id("struggle");

    public static StrugglePayload read(FriendlyByteBuf buf) {
        return new StrugglePayload();
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
