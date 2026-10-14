package tacz_camo_armor.item;

public enum ArmorForm {
    CT("ct"),
    T("t"),
    US("us");

    private final String id;

    ArmorForm(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
