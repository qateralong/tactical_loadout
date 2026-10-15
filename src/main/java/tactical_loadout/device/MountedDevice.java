package tactical_loadout.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import tactical_loadout.registry.ModDataComponents;
import tactical_loadout.registry.ModItems;

public record MountedDevice(DeviceKind kind, int charge) {
    private static final Codec<DeviceKind> KIND_CODEC = Codec.STRING.comapFlatMap(MountedDevice::parseKind, DeviceKind::name);

    public static final Codec<MountedDevice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            KIND_CODEC.fieldOf("kind").forGetter(MountedDevice::kind),
            Codec.INT.fieldOf("charge").forGetter(MountedDevice::charge)
    ).apply(instance, MountedDevice::new));

    public static final StreamCodec<ByteBuf, MountedDevice> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(ByIdMap.continuous(DeviceKind::ordinal, DeviceKind.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
                    DeviceKind::ordinal), MountedDevice::kind,
            ByteBufCodecs.VAR_INT, MountedDevice::charge,
            MountedDevice::new);

    private static DataResult<DeviceKind> parseKind(String name) {
        for (DeviceKind kind : DeviceKind.values()) {
            if (kind.name().equals(name)) {
                return DataResult.success(kind);
            }
        }
        return DataResult.error(() -> "Unknown device kind: " + name);
    }

    public static MountedDevice of(ItemStack stack) {
        DeviceItem item = (DeviceItem) stack.getItem();
        return new MountedDevice(item.kind(), item.charge(stack));
    }

    public MountedDevice withCharge(int value) {
        return new MountedDevice(kind, Math.max(0, value));
    }

    public int clampedCharge() {
        return Math.min(charge, kind.maxCharge());
    }

    public boolean hasCharge() {
        return !kind.usesCharge() || charge > 0;
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(ModItems.device(kind).get());
        stack.set(ModDataComponents.CHARGE.get(), charge);
        return stack;
    }
}
