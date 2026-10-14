package tacz_camo_armor.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModItems;

public record MountedDevice(DeviceKind kind, int charge) {
    private static final Codec<DeviceKind> KIND_CODEC = Codec.STRING.xmap(DeviceKind::valueOf, DeviceKind::name);

    public static final Codec<MountedDevice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            KIND_CODEC.fieldOf("kind").forGetter(MountedDevice::kind),
            Codec.INT.fieldOf("charge").forGetter(MountedDevice::charge)
    ).apply(instance, MountedDevice::new));

    public static final StreamCodec<ByteBuf, MountedDevice> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(i -> DeviceKind.values()[i], DeviceKind::ordinal), MountedDevice::kind,
            ByteBufCodecs.VAR_INT, MountedDevice::charge,
            MountedDevice::new);

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
