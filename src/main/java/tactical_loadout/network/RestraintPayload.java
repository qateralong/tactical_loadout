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

/** Tells a player how many struggles they still need to break free; 0 means they are not tied. */
public record RestraintPayload(int remaining) implements CustomPacketPayload {
    //? if >=1.20.5 {
    public static final Type<RestraintPayload> TYPE = new Type<>(TacticalLoadout.id("restraint"));
    public static final StreamCodec<ByteBuf, RestraintPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(RestraintPayload::new, RestraintPayload::remaining);

    @Override
    public Type<RestraintPayload> type() {
        return TYPE;
    }
    //?} else {
    /*public static final ResourceLocation ID = TacticalLoadout.id("restraint");

    public static RestraintPayload read(FriendlyByteBuf buf) {
        return new RestraintPayload(buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(remaining);
    }
    *///?}
}
