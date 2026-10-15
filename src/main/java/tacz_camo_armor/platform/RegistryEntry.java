package tacz_camo_armor.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** A registered object that is resolved lazily, so it can be declared before the loader fills its registries. */
public class RegistryEntry<R, T extends R> implements Supplier<T> {
    private final ResourceKey<R> key;
    private final Supplier<? extends Holder<R>> holder;

    public RegistryEntry(ResourceKey<R> key, Supplier<? extends Holder<R>> holder) {
        this.key = key;
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) holder.get().value();
    }

    public Holder<R> holder() {
        return holder.get();
    }

    public ResourceKey<R> key() {
        return key;
    }

    public ResourceLocation id() {
        return key.location();
    }
}
