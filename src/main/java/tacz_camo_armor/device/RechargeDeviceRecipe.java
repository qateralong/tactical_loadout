package tacz_camo_armor.device;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModRecipes;

public class RechargeDeviceRecipe extends CustomRecipe {
    public RechargeDeviceRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack target(CraftingInput input) {
        ItemStack target = ItemStack.EMPTY;
        ItemStack refill = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.BATTERY.get()) || stack.is(ModItems.GAS_FILTER.get())) {
                if (!refill.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                refill = stack;
            } else if (target.isEmpty()) {
                target = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (target.isEmpty() || refill.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean battery = refill.is(ModItems.BATTERY.get());
        MountedDevice device = device(target, battery);
        if (device == null || !device.kind().usesCharge() || device.clampedCharge() >= device.kind().maxCharge()) {
            return ItemStack.EMPTY;
        }
        return target;
    }

    private static MountedDevice device(ItemStack stack, boolean battery) {
        if (stack.getItem() instanceof DeviceItem item) {
            return item.kind().isNvg() == battery ? MountedDevice.of(stack) : null;
        }
        if (Devices.isHelmet(stack)) {
            return stack.get(battery ? ModDataComponents.NVG.get() : ModDataComponents.GAS_MASK.get());
        }
        return null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !target(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack target = target(input);
        if (target.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = target.copyWithCount(1);
        if (result.getItem() instanceof DeviceItem item) {
            result.set(ModDataComponents.CHARGE.get(), item.kind().maxCharge());
        } else {
            for (DeviceKind kind : new DeviceKind[]{DeviceKind.PVS14, DeviceKind.GAS_MASK}) {
                MountedDevice device = result.get(Devices.component(kind));
                boolean refillMatches = input.items().stream().anyMatch(s -> s.is(kind.isNvg() ? ModItems.BATTERY.get() : ModItems.GAS_FILTER.get()));
                if (device != null && refillMatches) {
                    result.set(Devices.component(kind), device.withCharge(device.kind().maxCharge()));
                }
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RECHARGE_DEVICE.get();
    }
}
