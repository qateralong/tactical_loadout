package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

public final class ModSounds {
    public static final RegistryEntry<SoundEvent, SoundEvent> DECOY_SHOT = register("grenade.decoy_shot");
    public static final RegistryEntry<SoundEvent, SoundEvent> GRENADE_PIN = register("grenade.pin");

    private ModSounds() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    private static RegistryEntry<SoundEvent, SoundEvent> register(String name) {
        return Platform.INSTANCE.register(Registries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(TacticalLoadout.id(name)));
    }
}
