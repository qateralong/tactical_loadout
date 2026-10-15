package tacz_camo_armor.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import tacz_camo_armor.device.MountedDevice;
import tacz_camo_armor.platform.Platform;
import tacz_camo_armor.platform.RegistryEntry;

import java.util.function.UnaryOperator;

public final class ModDataComponents {
    public static final RegistryEntry<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            register("charge", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final RegistryEntry<DataComponentType<?>, DataComponentType<MountedDevice>> NVG =
            register("nvg", b -> b.persistent(MountedDevice.CODEC).networkSynchronized(MountedDevice.STREAM_CODEC));
    public static final RegistryEntry<DataComponentType<?>, DataComponentType<MountedDevice>> GAS_MASK =
            register("gas_mask", b -> b.persistent(MountedDevice.CODEC).networkSynchronized(MountedDevice.STREAM_CODEC));
    public static final RegistryEntry<DataComponentType<?>, DataComponentType<Boolean>> NVG_DOWN =
            register("nvg_down", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private ModDataComponents() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    private static <T> RegistryEntry<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Platform.INSTANCE.register(Registries.DATA_COMPONENT_TYPE, name, () -> builder.apply(DataComponentType.builder()).build());
    }
}
