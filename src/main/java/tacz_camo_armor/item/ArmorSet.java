package tacz_camo_armor.item;

public enum ArmorSet {
    CT("ct", ArmorForm.CT, "blue", false),
    T("t", ArmorForm.T, "beige", false),
    US_WOODLAND("us_woodland", ArmorForm.US, "woodland", true),
    US_DESERT("us_desert", ArmorForm.US, "sand", true),
    US_GREEN("us_green", ArmorForm.US, "olive", true);

    private final String id;
    private final ArmorForm form;
    private final String fabricColor;
    private final boolean camouflage;

    ArmorSet(String id, ArmorForm form, String fabricColor, boolean camouflage) {
        this.id = id;
        this.form = form;
        this.fabricColor = fabricColor;
        this.camouflage = camouflage;
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
        return camouflage;
    }
}
