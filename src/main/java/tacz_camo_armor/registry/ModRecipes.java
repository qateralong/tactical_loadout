package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.AttachDeviceRecipe;
import tacz_camo_armor.device.DetachDeviceRecipe;
import tacz_camo_armor.device.RechargeDeviceRecipe;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TaczCamoArmor.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<AttachDeviceRecipe>> ATTACH_DEVICE =
            SERIALIZERS.register("attach_device", () -> new SimpleCraftingRecipeSerializer<>(AttachDeviceRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<DetachDeviceRecipe>> DETACH_DEVICE =
            SERIALIZERS.register("detach_device", () -> new SimpleCraftingRecipeSerializer<>(DetachDeviceRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RechargeDeviceRecipe>> RECHARGE_DEVICE =
            SERIALIZERS.register("recharge_device", () -> new SimpleCraftingRecipeSerializer<>(RechargeDeviceRecipe::new));

    private ModRecipes() {
    }
}
