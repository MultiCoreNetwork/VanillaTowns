package network.multicore.vt.persistence;

public enum HibernateHbm2DdlAutoMode {
    CREATE("create"),
    UPDATE("update"),
    VALIDATE("validate"),
    CREATE_DROP("create-drop"),
    NONE("none");

    private final String value;

    HibernateHbm2DdlAutoMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
