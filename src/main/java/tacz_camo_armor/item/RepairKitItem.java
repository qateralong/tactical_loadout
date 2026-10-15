package tacz_camo_armor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;

import java.util.List;

/** Repairs any tactical armor piece in an anvil, a fixed share of durability per kit for one level each. */
@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public class RepairKitItem extends Item {
    public RepairKitItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".repair_kit",
                Math.round(ServerConfig.repairKitFraction() * 100)).withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack armor = event.getLeft();
        ItemStack kits = event.getRight();
        if (!(armor.getItem() instanceof TacticalArmorItem) || !(kits.getItem() instanceof RepairKitItem) || !armor.isDamaged()) {
            return;
        }
        int perKit = Math.max(1, (int) Math.round(armor.getMaxDamage() * ServerConfig.repairKitFraction()));
        int used = Math.min(kits.getCount(), (armor.getDamageValue() + perKit - 1) / perKit);
        ItemStack result = armor.copy();
        result.setDamageValue(Math.max(0, armor.getDamageValue() - used * perKit));
        int cost = used;
        String name = event.getName();
        if (name == null || StringUtil.isBlank(name)) {
            if (armor.has(DataComponents.CUSTOM_NAME)) {
                result.remove(DataComponents.CUSTOM_NAME);
                cost++;
            }
        } else if (!name.equals(armor.getHoverName().getString())) {
            result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            cost++;
        }
        event.setOutput(result);
        event.setCost(cost);
        event.setMaterialCost(used);
    }
}
