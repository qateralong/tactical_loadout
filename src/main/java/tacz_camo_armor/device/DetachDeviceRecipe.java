package tacz_camo_armor.device;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModRecipes;

public class DetachDeviceRecipe extends CustomRecipe {
    public DetachDeviceRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static int helmetIndex(CraftingInput input) {
        int index = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (index >= 0 || !Devices.isHelmet(stack)
                    || (!stack.has(ModDataComponents.NVG.get()) && !stack.has(ModDataComponents.GAS_MASK.get()))) {
                return -1;
            }
            index = i;
        }
        return index;
    }

    private static MountedDevice removed(ItemStack helmet) {
        MountedDevice nvg = helmet.get(ModDataComponents.NVG.get());
        return nvg != null ? nvg : helmet.get(ModDataComponents.GAS_MASK.get());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return helmetIndex(input) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int index = helmetIndex(input);
        if (index < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = input.getItem(index).copyWithCount(1);
        MountedDevice device = removed(result);
        result.remove(Devices.component(device.kind()));
        if (device.kind().isNvg()) {
            result.remove(ModDataComponents.NVG_DOWN.get());
        }
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        int index = helmetIndex(input);
        if (index >= 0) {
            remaining.set(index, removed(input.getItem(index)).toStack());
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DETACH_DEVICE.get();
    }
}
