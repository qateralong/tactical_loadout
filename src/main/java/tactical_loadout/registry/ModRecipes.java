package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CraftingRecipe;
//? if >=1.21 {
import net.minecraft.world.item.crafting.CraftingBookCategory;
//?}
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import tactical_loadout.device.AttachDeviceRecipe;
import tactical_loadout.device.DetachDeviceRecipe;
import tactical_loadout.device.RechargeDeviceRecipe;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

//? if >=1.21 {
import java.util.function.Function;
//?}

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
            //? if >=1.21 {
            String name, Function<CraftingBookCategory, T> factory) {
            //?} else {
            /*String name, SimpleCraftingRecipeSerializer.Factory<T> factory) {
            *///?}
        //? if >=1.21 {
        return Platform.INSTANCE.register(Registries.RECIPE_SERIALIZER, name, () -> new SimpleCraftingRecipeSerializer<>(factory::apply));
        //?} else {
        /*return Platform.INSTANCE.register(Registries.RECIPE_SERIALIZER, name, () -> new SimpleCraftingRecipeSerializer<>(factory));
        *///?}
    }
}
