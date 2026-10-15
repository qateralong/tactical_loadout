package tactical_loadout.device;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tactical_loadout.registry.ModRecipes;

public class AttachDeviceRecipe extends CustomRecipe {
    public AttachDeviceRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack[] find(CraftingInput input) {
        ItemStack helmet = ItemStack.EMPTY;
        ItemStack device = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (Devices.isHelmet(stack) && helmet.isEmpty()) {
                helmet = stack;
            } else if (stack.getItem() instanceof DeviceItem && device.isEmpty()) {
                device = stack;
            } else {
                return null;
            }
        }
        if (helmet.isEmpty() || device.isEmpty() || !Devices.accepts(helmet, ((DeviceItem) device.getItem()).kind())) {
            return null;
        }
        return new ItemStack[]{helmet, device};
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return find(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack[] found = find(input);
        if (found == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = found[0].copyWithCount(1);
        MountedDevice device = MountedDevice.of(found[1]);
        result.set(Devices.component(device.kind()), device);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ATTACH_DEVICE.get();
    }
}
