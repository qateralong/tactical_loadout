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

/** A player's downed state for everyone nearby: ticks left before bleeding out, 0 when they are not down. */
public record DownedPayload(int entityId, int ticks) implements CustomPacketPayload {
    //? if >=1.20.5 {
    public static final Type<DownedPayload> TYPE = new Type<>(TacticalLoadout.id("downed"));
    public static final StreamCodec<ByteBuf, DownedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DownedPayload::entityId,
            ByteBufCodecs.VAR_INT, DownedPayload::ticks,
            DownedPayload::new);

    @Override
    public Type<DownedPayload> type() {
        return TYPE;
    }
    //?} else {
    /*public static final ResourceLocation ID = TacticalLoadout.id("downed");

    public static DownedPayload read(FriendlyByteBuf buf) {
        return new DownedPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(ticks);
    }
    *///?}
}
