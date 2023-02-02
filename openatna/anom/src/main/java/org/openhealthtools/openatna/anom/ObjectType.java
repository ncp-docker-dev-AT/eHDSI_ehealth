package org.openhealthtools.openatna.anom;

/**
 * <p>
 * Value   Meaning
 * ----- -------------
 * 1       Person
 * 2       System Object
 * 3       Organization
 * 4       Other
 * </p>
 */
public enum ObjectType {
    PERSON(1),
    SYSTEM_OBJECT(2),
    ORGANIZATION(3),
    OTHER(4);

    private final int value;

    ObjectType(int value) {
        this.value = value;
    }

    public static ObjectType getType(int type) {
        for (ObjectType o : values()) {
            if (o.value() == type) {
                return o;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }
}
