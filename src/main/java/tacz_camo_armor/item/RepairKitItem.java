package tacz_camo_armor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;

import java.util.List;

/** Repairs any tactical armor piece in an anvil, a fixed share of durability per kit for one level each. */
public class RepairKitItem extends Item {
    public record AnvilResult(ItemStack output, int cost, int kitsUsed) {
    }

    public RepairKitItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".repair_kit",
                Math.round(ServerConfig.repairKitFraction() * 100)).withStyle(ChatFormatting.GRAY));
    }

    /** The anvil output for repairing {@code armor} with {@code kits}, or null when this is not a kit repair. */
    @Nullable
    public static AnvilResult repair(ItemStack armor, ItemStack kits, @Nullable String name) {
        if (!(armor.getItem() instanceof TacticalArmorItem) || !(kits.getItem() instanceof RepairKitItem) || !armor.isDamaged()) {
            return null;
        }
        int perKit = Math.max(1, (int) Math.round(armor.getMaxDamage() * ServerConfig.repairKitFraction()));
        int used = Math.min(kits.getCount(), (armor.getDamageValue() + perKit - 1) / perKit);
        ItemStack result = armor.copy();
        result.setDamageValue(Math.max(0, armor.getDamageValue() - used * perKit));
        int cost = used;
        if (name == null || StringUtil.isBlank(name)) {
            if (armor.has(DataComponents.CUSTOM_NAME)) {
                result.remove(DataComponents.CUSTOM_NAME);
                cost++;
            }
        } else if (!name.equals(armor.getHoverName().getString())) {
            result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            cost++;
        }
        return new AnvilResult(result, cost, used);
    }
}
