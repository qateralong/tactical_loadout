package tacz_camo_armor.grenade;

public enum GrenadeKind {
    FLASH("flashbang", 30),
    SMOKE("smoke_grenade", 40),
    FRAG("frag_grenade", 60),
    HE("he_grenade", 60);

    private final String id;
    private final int fuseTicks;

    GrenadeKind(String id, int fuseTicks) {
        this.id = id;
        this.fuseTicks = fuseTicks;
    }

    public String id() {
        return id;
    }

    public int fuseTicks() {
        return fuseTicks;
    }
}
