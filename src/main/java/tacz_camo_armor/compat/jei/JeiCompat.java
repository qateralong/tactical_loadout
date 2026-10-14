package tacz_camo_armor.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.device.Devices;
import tacz_camo_armor.device.MountedDevice;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiCompat implements IModPlugin {
    private static final String INFO = "jei." + TaczCamoArmor.MOD_ID + ".";

    @Override
    public ResourceLocation getPluginUid() {
        return TaczCamoArmor.id("jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (ArmorSet set : ArmorSet.values()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ArmorTier tier : set.tiers()) {
                for (ArmorPiece piece : ArmorPiece.values()) {
                    stacks.add(new ItemStack(ModItems.armor(set, tier, piece).get()));
                }
            }
            registration.addItemStackInfo(stacks, setInfo(set));
        }
        long seconds = ServerConfig.nvgBatterySeconds();
        Component key = Component.keybind("key." + TaczCamoArmor.MOD_ID + ".nvg");
        registration.addItemStackInfo(new ItemStack(ModItems.device(DeviceKind.PVS14).get()),
                Component.translatable(INFO + "nvg", key, seconds), Component.translatable(INFO + "pvs14"),
                Component.translatable(INFO + "detach"));
        registration.addItemStackInfo(new ItemStack(ModItems.device(DeviceKind.GPNVG18).get()),
                Component.translatable(INFO + "nvg", key, seconds), Component.translatable(INFO + "gpnvg18"),
                Component.translatable(INFO + "detach"));
        registration.addItemStackInfo(new ItemStack(ModItems.device(DeviceKind.GPNVG18_GREEN).get()),
                Component.translatable(INFO + "nvg", key, seconds), Component.translatable(INFO + "gpnvg18_green"),
                Component.translatable(INFO + "detach"));
        registration.addItemStackInfo(new ItemStack(ModItems.device(DeviceKind.THERMAL).get()),
                Component.translatable(INFO + "nvg", key, seconds), Component.translatable(INFO + "thermal"),
                Component.translatable(INFO + "detach"));
        registration.addItemStackInfo(new ItemStack(ModItems.device(DeviceKind.GAS_MASK).get()),
                Component.translatable(INFO + "gas_mask", ServerConfig.gasFilterSeconds()), Component.translatable(INFO + "detach"));
        registration.addItemStackInfo(new ItemStack(ModItems.BATTERY.get()), Component.translatable(INFO + "battery"));
        for (GrenadeKind kind : GrenadeKind.values()) {
            registration.addItemStackInfo(new ItemStack(ModItems.grenade(kind).get()),
                    Component.translatable(INFO + "grenade." + kind.id()), Component.translatable(INFO + "grenade_throw"));
        }
        registration.addItemStackInfo(new ItemStack(ModItems.GAS_FILTER.get()), Component.translatable(INFO + "gas_filter"));
        registration.addRecipes(RecipeTypes.CRAFTING, deviceRecipes());
    }

    private static Component[] setInfo(ArmorSet set) {
        List<Component> lines = new ArrayList<>();
        if (set.tiers().size() == 1) {
            lines.add(Component.translatable(INFO + "protection_single", percent(ServerConfig.bulletReduction(set.tiers().get(0)))));
        } else {
            lines.add(Component.translatable(INFO + "protection", percent(ServerConfig.bulletReduction(ArmorTier.LIGHT)),
                    percent(ServerConfig.bulletReduction(ArmorTier.MEDIUM)), percent(ServerConfig.bulletReduction(ArmorTier.HEAVY))));
            lines.add(Component.translatable(INFO + "heavy", percent(ServerConfig.heavySlowdownPerPiece())));
        }
        lines.add(Component.translatable(INFO + "zones"));
        if (set.camo() != null) {
            double strength = set.camo().strength();
            lines.add(Component.translatable(INFO + "camo", percent(Math.min(0.95, ServerConfig.detectionReduction() * strength)),
                    percent(Math.min(0.95, ServerConfig.sneakingDetectionReduction() * strength))));
            lines.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".camo_biome." + set.id()));
        } else {
            lines.add(Component.translatable(INFO + "no_camo"));
        }
        lines.add(Component.translatable(INFO + (Devices.NVG_FORMS.contains(set.form()) ? "nvg_fits" : "mask_fits")));
        return lines.toArray(Component[]::new);
    }

    private static long percent(double value) {
        return Math.round(value * 100);
    }

    private static List<RecipeHolder<CraftingRecipe>> deviceRecipes() {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        ItemStack helmet = new ItemStack(ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.MEDIUM, ArmorPiece.HELMET).get());
        Ingredient nvgHelmets = Ingredient.of(ModItems.allArmor().stream().map(h -> h.get())
                .filter(i -> i.piece() == ArmorPiece.HELMET && Devices.NVG_FORMS.contains(i.set().form()))
                .toArray(TacticalArmorItem[]::new));
        Ingredient allHelmets = Ingredient.of(ModItems.allArmor().stream().map(h -> h.get())
                .filter(i -> i.piece() == ArmorPiece.HELMET).toArray(TacticalArmorItem[]::new));
        for (DeviceKind kind : DeviceKind.values()) {
            ItemStack device = new ItemStack(ModItems.device(kind).get());
            ItemStack mounted = helmet.copy();
            mounted.set(Devices.component(kind), MountedDevice.of(device));
            recipes.add(shapeless("attach_" + kind.id(), mounted, kind.isNvg() ? nvgHelmets : allHelmets, Ingredient.of(device)));
            recipes.add(shapeless("detach_" + kind.id(), helmet, Ingredient.of(mounted)));
            ItemStack empty = device.copy();
            empty.set(ModDataComponents.CHARGE.get(), 0);
            ItemStack refill = new ItemStack(kind.isNvg() ? ModItems.BATTERY.get() : ModItems.GAS_FILTER.get());
            if (kind.usesCharge()) {
                recipes.add(shapeless("recharge_" + kind.id(), device, Ingredient.of(empty), Ingredient.of(refill)));
            }
        }
        return recipes;
    }

    private static RecipeHolder<CraftingRecipe> shapeless(String name, ItemStack result, Ingredient... ingredients) {
        NonNullList<Ingredient> list = NonNullList.of(Ingredient.EMPTY, ingredients);
        return new RecipeHolder<>(TaczCamoArmor.id("jei/" + name),
                new ShapelessRecipe("", CraftingBookCategory.EQUIPMENT, result, list));
    }
}
