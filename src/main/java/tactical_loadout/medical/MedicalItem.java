package tactical_loadout.medical;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ServerConfig;

import java.util.List;

/** Medkits heal and stop bleeding; tourniquets only stop bleeding, but much faster. */
public class MedicalItem extends Item {
    public enum Kind {
        MEDKIT("medkit", 80, 4),
        TOURNIQUET("tourniquet", 30, 8);

        private final String id;
        private final int useTicks;
        private final int stackSize;

        Kind(String id, int useTicks, int stackSize) {
            this.id = id;
            this.useTicks = useTicks;
            this.stackSize = stackSize;
        }

        public String id() {
            return id;
        }

        public int useTicks() {
            return useTicks;
        }
    }

    private final Kind kind;

    public MedicalItem(Kind kind, Properties properties) {
        super(properties.stacksTo(kind.stackSize));
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    private boolean helps(LivingEntity entity) {
        return Medical.isBleeding(entity) || kind == Kind.MEDKIT && entity.getHealth() < entity.getMaxHealth();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!helps(player)) {
            player.displayClientMessage(Component.translatable("message." + TacticalLoadout.MOD_ID + "." + kind.id + ".not_needed"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return kind.useTicks;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && remaining % 15 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.6F,
                    0.8F + level.getRandom().nextFloat() * 0.3F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            Medical.stopBleeding(entity);
            if (kind == Kind.MEDKIT) {
                entity.heal((float) ServerConfig.medkitHealth());
            }
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS,
                    1.0F, 1.0F);
            if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + "." + kind.id, kind.useTicks / 20.0,
                Math.round(ServerConfig.medkitHealth() / 2)).withStyle(ChatFormatting.GRAY));
    }
}
