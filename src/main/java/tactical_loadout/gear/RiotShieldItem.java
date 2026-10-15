package tactical_loadout.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.registry.ModItems;

import java.util.List;

/** A ballistic shield: blocks bullets and other attacks from the front like a vanilla shield, and lasts much longer. */
public class RiotShieldItem extends ShieldItem {
    public RiotShieldItem(Properties properties) {
        super(properties.durability(1500));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.CERAMIC_PLATE.get());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".riot_shield").withStyle(ChatFormatting.GRAY));
    }
}
