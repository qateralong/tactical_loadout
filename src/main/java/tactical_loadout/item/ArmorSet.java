package tactical_loadout.item;

import org.jetbrains.annotations.Nullable;
import tactical_loadout.camo.CamoPattern;

import java.util.List;

public enum ArmorSet {
    CT("ct", ArmorForm.CT, "blue", null),
    T("t", ArmorForm.T, "beige", null),
    US_WOODLAND("us_woodland", ArmorForm.US, "woodland", CamoPattern.WOODLAND),
    US_DESERT("us_desert", ArmorForm.US, "sand", CamoPattern.DESERT),
    US_GREEN("us_green", ArmorForm.US, "olive", CamoPattern.OLIVE),
    US_MULTICAM("us_multicam", ArmorForm.US, "multicam", CamoPattern.MULTICAM),
    US_SNOW("us_snow", ArmorForm.US, "snow", CamoPattern.SNOW),
    US_UCP("us_ucp", ArmorForm.US, "ucp", CamoPattern.UCP),
    US_TIGER("us_tiger", ArmorForm.US, "tiger_stripe", CamoPattern.TIGER),
    RU_EMR("ru_emr", ArmorForm.RU, "emr", CamoPattern.EMR),
    RU_FLORA("ru_flora", ArmorForm.RU, "flora", CamoPattern.FLORA),
    GHILLIE("ghillie", ArmorForm.GHILLIE, "ghillie", CamoPattern.GHILLIE),
    GHILLIE_SNOW("ghillie_snow", ArmorForm.GHILLIE, "snow_ghillie", CamoPattern.GHILLIE_SNOW),
    GHILLIE_DESERT("ghillie_desert", ArmorForm.GHILLIE, "desert_ghillie", CamoPattern.GHILLIE_DESERT),
    SWAT("swat", ArmorForm.SWAT, "black", null);

    private final String id;
    private final ArmorForm form;
    private final String fabricColor;
    @Nullable
    private final CamoPattern camo;

    ArmorSet(String id, ArmorForm form, String fabricColor, @Nullable CamoPattern camo) {
        this.id = id;
        this.form = form;
        this.fabricColor = fabricColor;
        this.camo = camo;
    }

    public String id() {
        return id;
    }

    public ArmorForm form() {
        return form;
    }

    public String fabricId() {
        return fabricColor + "_fabric";
    }

    public boolean hasCamouflage() {
        return camo != null;
    }

    @Nullable
    public CamoPattern camo() {
        return camo;
    }

    public List<ArmorTier> tiers() {
        return form == ArmorForm.GHILLIE ? List.of(ArmorTier.LIGHT) : List.of(ArmorTier.values());
    }
}
