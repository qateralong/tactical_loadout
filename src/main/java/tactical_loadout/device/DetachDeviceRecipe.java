package tactical_loadout.device;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tactical_loadout.registry.ModDataComponents;
import tactical_loadout.registry.ModRecipes;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingInput;
//?} else {
/*import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
*///?}

public class DetachDeviceRecipe extends CustomRecipe {
    //? if >=1.21 {
    public DetachDeviceRecipe(CraftingBookCategory category) {
        super(category);
    }
    //?} else {
    /*public DetachDeviceRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }
    *///?}

    private static int helmetIndex(CraftingInput input) {
        int index = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (index >= 0 || !Devices.isHelmet(stack)
                    || (!ModDataComponents.NVG.has(stack) && !ModDataComponents.GAS_MASK.has(stack))) {
                return -1;
            }
            index = i;
        }
        return index;
    }

    private static MountedDevice removed(ItemStack helmet) {
        MountedDevice nvg = ModDataComponents.NVG.get(helmet);
        return nvg != null ? nvg : ModDataComponents.GAS_MASK.get(helmet);
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
        Devices.component(device.kind()).remove(result);
        if (device.kind().isNvg()) {
            ModDataComponents.NVG_DOWN.remove(result);
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
