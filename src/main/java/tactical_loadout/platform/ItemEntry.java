package tactical_loadout.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public class ItemEntry<T extends Item> extends RegistryEntry<Item, T> implements ItemLike {
    public ItemEntry(ResourceKey<Item> key, Supplier<? extends Holder<Item>> holder) {
        super(key, holder);
    }

    @Override
    public Item asItem() {
        return get();
    }

    public ItemStack toStack() {
        return new ItemStack(get());
    }
}
