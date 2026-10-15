package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import tactical_loadout.medical.BleedingEffect;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

public final class ModEffects {
    public static final RegistryEntry<MobEffect, BleedingEffect> BLEEDING =
            Platform.INSTANCE.register(Registries.MOB_EFFECT, "bleeding", BleedingEffect::new);

    private ModEffects() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
