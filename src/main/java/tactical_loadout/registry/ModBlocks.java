package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import tactical_loadout.gear.TripwireAlarmBlock;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

public final class ModBlocks {
    public static final RegistryEntry<Block, TripwireAlarmBlock> TRIPWIRE_ALARM = Platform.INSTANCE.register(Registries.BLOCK, "tripwire_alarm",
            () -> new TripwireAlarmBlock(BlockBehaviour.Properties.of().noCollission().instabreak().noOcclusion()
                    .sound(SoundType.CHAIN).pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
