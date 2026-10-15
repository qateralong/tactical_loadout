package tactical_loadout.network;

//? if <1.20.5 {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

// Stand-in for the vanilla payload interface that arrived in 1.20.5: each payload names its channel and writes itself.
public interface CustomPacketPayload {
    ResourceLocation id();

    void write(FriendlyByteBuf buf);
}
*///?}
