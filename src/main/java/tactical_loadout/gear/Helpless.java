package tactical_loadout.gear;

import net.minecraft.world.entity.player.Player;
import tactical_loadout.medical.Downed;

/** Players who may not attack, use items, shoot or build right now. */
public final class Helpless {
    private Helpless() {
    }

    public static boolean is(Player player) {
        return Restraints.isTied(player) || Downed.isDowned(player);
    }
}
