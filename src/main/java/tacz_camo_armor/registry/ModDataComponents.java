package tacz_camo_armor.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.MountedDevice;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TaczCamoArmor.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            COMPONENTS.registerComponentType("charge", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MountedDevice>> NVG =
            COMPONENTS.registerComponentType("nvg", b -> b.persistent(MountedDevice.CODEC).networkSynchronized(MountedDevice.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MountedDevice>> GAS_MASK =
            COMPONENTS.registerComponentType("gas_mask", b -> b.persistent(MountedDevice.CODEC).networkSynchronized(MountedDevice.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NVG_DOWN =
            COMPONENTS.registerComponentType("nvg_down", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private ModDataComponents() {
    }
}
