package tacz_camo_armor.grenade;

public enum GrenadeKind {
    FRAG("frag_grenade", 60),
    HE("he_grenade", 60),
    FLASH("flashbang", 30),
    SMOKE("smoke_grenade", 40),
    MOLOTOV("molotov", 40),
    INCENDIARY("incendiary_grenade", 40),
    DECOY("decoy_grenade", 40);

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

    public boolean isFire() {
        return this == MOLOTOV || this == INCENDIARY;
    }
}
