package tactical_loadout.registry;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tactical_loadout.device.MountedDevice;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import tactical_loadout.TacticalLoadout;
*///?}

/** Values stored on item stacks: data components since 1.20.5, NBT tags before. */
public final class ModDataComponents {
    //? if >=1.20.5 {
    public static final Key<Integer> CHARGE = new Key<>("charge", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final Key<MountedDevice> NVG = new Key<>("nvg", MountedDevice.CODEC, MountedDevice.STREAM_CODEC);
    public static final Key<MountedDevice> GAS_MASK = new Key<>("gas_mask", MountedDevice.CODEC, MountedDevice.STREAM_CODEC);
    public static final Key<Boolean> NVG_DOWN = new Key<>("nvg_down", Codec.BOOL, ByteBufCodecs.BOOL);
    //?} else {
    /*public static final Key<Integer> CHARGE = new Key<>("charge", Codec.INT);
    public static final Key<MountedDevice> NVG = new Key<>("nvg", MountedDevice.CODEC);
    public static final Key<MountedDevice> GAS_MASK = new Key<>("gas_mask", MountedDevice.CODEC);
    public static final Key<Boolean> NVG_DOWN = new Key<>("nvg_down", Codec.BOOL);
    *///?}

    private ModDataComponents() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    public static final class Key<T> {
        //? if >=1.20.5 {
        private final RegistryEntry<DataComponentType<?>, DataComponentType<T>> type;

        private Key(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            type = Platform.INSTANCE.register(Registries.DATA_COMPONENT_TYPE, name,
                    () -> DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
        }

        public DataComponentType<T> type() {
            return type.get();
        }

        @Nullable
        public T get(ItemStack stack) {
            return stack.get(type.get());
        }

        public void set(ItemStack stack, T value) {
            stack.set(type.get(), value);
        }

        public void remove(ItemStack stack) {
            stack.remove(type.get());
        }

        public boolean has(ItemStack stack) {
            return stack.has(type.get());
        }
        //?} else {
        /*private final String name;
        private final Codec<T> codec;

        private Key(String name, Codec<T> codec) {
            this.name = TacticalLoadout.MOD_ID + ":" + name;
            this.codec = codec;
        }

        @Nullable
        public T get(ItemStack stack) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains(name)) {
                return null;
            }
            return codec.parse(NbtOps.INSTANCE, tag.get(name)).result().orElse(null);
        }

        public void set(ItemStack stack, T value) {
            codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(tag -> stack.getOrCreateTag().put(name, tag));
        }

        public void remove(ItemStack stack) {
            stack.removeTagKey(name);
        }

        public boolean has(ItemStack stack) {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.contains(name);
        }
        *///?}

        public T getOrDefault(ItemStack stack, T fallback) {
            T value = get(stack);
            return value == null ? fallback : value;
        }
    }
}
