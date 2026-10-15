package tactical_loadout.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ServerConfig;

import java.util.List;

public class ZipTiesItem extends Item {
    public ZipTiesItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer binder && target instanceof ServerPlayer victim) {
            Restraints.tieClick(binder, victim, stack);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".zip_ties", Restraints.CLICKS, ServerConfig.zipTieStruggles())
                .withStyle(ChatFormatting.GRAY));
    }
}
