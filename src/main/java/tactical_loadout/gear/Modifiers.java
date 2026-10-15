package tactical_loadout.gear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
//? if >=1.21 {
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
//?} else {
/*import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
*///?}

/**
 * Temporary speed and jump changes under a named modifier; a value of 0 removes it. Before 1.21 players have no jump
 * attribute, so LivingEntityJumpMixin handles jumping there instead.
 */
public final class Modifiers {
    private Modifiers() {
    }

    /** Adds {@code fraction} of the total walking speed; -1 stops walking. */
    public static void speed(LivingEntity entity, ResourceLocation id, double fraction) {
        set(entity, Attributes.MOVEMENT_SPEED, id, fraction);
    }

    public static void jump(LivingEntity entity, ResourceLocation id, double fraction) {
        //? if >=1.21 {
        set(entity, Attributes.JUMP_STRENGTH, id, fraction);
        //?}
    }

    //? if >=1.21 {
    private static void set(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double fraction) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (fraction != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, fraction, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
    //?} else {
    /*private static void set(LivingEntity entity, Attribute attribute, ResourceLocation id, double fraction) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        UUID uuid = UUID.nameUUIDFromBytes(id.toString().getBytes());
        instance.removeModifier(uuid);
        if (fraction != 0.0) {
            instance.addTransientModifier(new AttributeModifier(uuid, id.toString(), fraction, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
    *///?}
}
