package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TaczCamoArmor.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DECOY_SHOT = register("grenade.decoy_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRENADE_PIN = register("grenade.pin");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TaczCamoArmor.id(name)));
    }
}
