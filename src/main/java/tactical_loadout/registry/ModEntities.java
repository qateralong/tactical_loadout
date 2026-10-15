package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;
import tactical_loadout.grenade.GrenadeEntity;

public final class ModEntities {
    public static final RegistryEntry<EntityType<?>, EntityType<GrenadeEntity>> GRENADE =
            Platform.INSTANCE.register(Registries.ENTITY_TYPE, "grenade",
                () -> EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2).build("grenade"));

    private ModEntities() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
