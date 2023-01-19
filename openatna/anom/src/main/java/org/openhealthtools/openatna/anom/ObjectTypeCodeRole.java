package org.openhealthtools.openatna.anom;

/**
 * <p>
 * Value Meaning              Participant Object Type Codes
 * ----- -------------------- ----------------------------------
 * 1   Patient              1 - Person
 * 2   Location             3 - Organization
 * 3   Report               2 - System Object
 * 4   Resource             1 - Person
 * 3 - Organization
 * 5   Master file          2 - System Object
 * 6   User                 1 - Person
 * 2 - System Object (non-human user)
 * 7   List                 2 - System Object
 * 8   Doctor               1 - Person
 * 9   Subscriber           3 - Organization
 * 10  Guarantor            1 - Person
 * 3 - Organization
 * 11  Security User Entity 1 - Person
 * 2 - System Object
 * 12  Security User Group  2 - System Object
 * 13  Security Resource    2 - System Object
 * 14  Security Granularity 2 - System Object
 * Definition
 * 15  Provider             1 - Person
 * 3 - Organization
 * 16  Data Destination     2 - System Object
 * 17  Data Repository      2 - System Object
 * 18  Schedule             2 - System Object
 * 19  Customer             3 - Organization
 * 20  Job                  2 - System Object
 * 21  Job Stream           2 - System Object
 * 22  Table                2 - System Object
 * 23  Routing Criteria     2 - System Object
 * 24  Query                2 - System Object
 * </p>
 */
public enum ObjectTypeCodeRole {

    /**
     * Value Meaning              Participant Object Type Codes
     * ----- -------------------- ----------------------------------
     * 1   Patient              1 - Person
     * 2   Location             3 - Organization
     * 3   Report               2 - System Object
     * 4   Resource             1 - Person
     * 3 - Organization
     * 5   Master file          2 - System Object
     * 6   User                 1 - Person
     * 2 - System Object (non-human user)
     * 7   List                 2 - System Object
     * 8   Doctor               1 - Person
     * 9   Subscriber           3 - Organization
     * 10  Guarantor            1 - Person
     * 3 - Organization
     * 11  Security User Entity 1 - Person
     * 2 - System Object
     * 12  Security User Group  2 - System Object
     * 13  Security Resource    2 - System Object
     * 14  Security Granularity 2 - System Object
     * Definition
     * 15  Provider             1 - Person
     * 3 - Organization
     * 16  Data Destination     2 - System Object
     * 17  Data Repository      2 - System Object
     * 18  Schedule             2 - System Object
     * 19  Customer             3 - Organization
     * 20  Job                  2 - System Object
     * 21  Job Stream           2 - System Object
     * 22  Table                2 - System Object
     * 23  Routing Criteria     2 - System Object
     * 24  Query                2 - System Object
     */

    PATIENT(1),
    LOCATION(2),
    REPORT(3),
    RESOURCE(4),
    MASTER_FILE(5),
    USER(6),
    LIST(7),
    DOCTOR(8),
    SUBSCRIBER(9),
    GUARANTOR(10),
    SECURITY_USER_ENTITY(11),
    SECURITY_USER_GROUP(12),
    SECURITY_RESOURCE(13),
    SECURITY_GRANULARITY_DEFINITION(14),
    PROVIDER(15),
    DATA_DESTINATION(16),
    DATA_REPOSITORY(17),
    SCHEDULE(18),
    CUSTOMER(19),
    JOB(20),
    JOB_STREAM(21),
    TABLE(22),
    ROUTING_CRITERIA(23),
    QUERY(24);

    private final int value;

    ObjectTypeCodeRole(int value) {
        this.value = value;
    }

    public static ObjectTypeCodeRole getRole(int role) {
        for (ObjectTypeCodeRole o : values()) {
            if (o.value() == role) {
                return o;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }
}
