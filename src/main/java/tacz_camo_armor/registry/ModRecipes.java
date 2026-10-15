package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import tacz_camo_armor.device.AttachDeviceRecipe;
import tacz_camo_armor.device.DetachDeviceRecipe;
import tacz_camo_armor.device.RechargeDeviceRecipe;
import tacz_camo_armor.platform.Platform;
import tacz_camo_armor.platform.RegistryEntry;

import java.util.function.Function;

public final class ModRecipes {
    public static final RegistryEntry<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<AttachDeviceRecipe>> ATTACH_DEVICE =
            register("attach_device", AttachDeviceRecipe::new);
    public static final RegistryEntry<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<DetachDeviceRecipe>> DETACH_DEVICE =
            register("detach_device", DetachDeviceRecipe::new);
    public static final RegistryEntry<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RechargeDeviceRecipe>> RECHARGE_DEVICE =
            register("recharge_device", RechargeDeviceRecipe::new);

    private ModRecipes() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    private static <T extends CraftingRecipe> RegistryEntry<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<T>> register(
            String name, Function<CraftingBookCategory, T> factory) {
        return Platform.INSTANCE.register(Registries.RECIPE_SERIALIZER, name, () -> new SimpleCraftingRecipeSerializer<>(factory::apply));
    }
}
