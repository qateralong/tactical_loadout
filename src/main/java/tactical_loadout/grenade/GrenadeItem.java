package tactical_loadout.grenade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
import net.minecraft.world.item.ProjectileItem;
//?}
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.registry.ModSounds;

import java.util.List;

//? if >=1.21 {
public class GrenadeItem extends Item implements ProjectileItem {
//?} else {
/*public class GrenadeItem extends Item {
*///?}
    public static final int FULL_CHARGE_TICKS = 20;
    private static final int USE_DURATION = 72000;
    private static final float MIN_VELOCITY = 0.35F;
    private static final float MAX_VELOCITY = 1.5F;
    private final GrenadeKind kind;

    public GrenadeItem(GrenadeKind kind, Properties properties) {
        super(properties.stacksTo(16));
        this.kind = kind;
    }

    public GrenadeKind kind() {
        return kind;
    }

    public static float charge(int ticksUsed) {
        return Math.min(1.0F, ticksUsed / (float) FULL_CHARGE_TICKS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isUsingItem()) {
            level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), ModSounds.GRENADE_PIN.get(), SoundSource.PLAYERS,
                    0.7F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    //? if >=1.21 {
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
    //?} else {
    /*public int getUseDuration(ItemStack stack) {
    *///?}
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        //? if >=1.21 {
        float charge = charge(getUseDuration(stack, entity) - timeLeft);
        //?} else {
        /*float charge = charge(getUseDuration(stack) - timeLeft);
        *///?}
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS,
                0.4F + 0.4F * charge, 0.6F - 0.2F * charge);
        if (!level.isClientSide) {
            GrenadeEntity grenade = new GrenadeEntity(level, player, stack);
            float velocity = MIN_VELOCITY + (MAX_VELOCITY - MIN_VELOCITY) * charge;
            grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
            level.addFreshEntity(grenade);
            ModAdvancements.award(player, ModAdvancements.ALL_GRENADES, kind.id());
        }
        player.getCooldowns().addCooldown(this, 20);
        //? if >=1.21 {
        stack.consume(1, player);
        //?} else {
        /*if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        *///?}
    }

    //? if >=1.21 {
    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new GrenadeEntity(level, pos, stack);
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder().power(0.9F).uncertainty(3.0F).build();
    }
    //?}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".grenade." + kind.id()).withStyle(ChatFormatting.GRAY));
    }
}
