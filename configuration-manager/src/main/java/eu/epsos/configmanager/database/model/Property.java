package eu.epsos.configmanager.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "property")
public class Property {

    @Id
    private String name;

    private String value;

    @Column(name = "is_smp")
    private boolean smp;

    public Property() {
        // Default constructor required by Hibernate
    }

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Property(String name, String value, boolean smp) {
        this.name = name;
        this.value = value;
        this.smp = smp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSmp() {
        return smp;
    }

    public void setSmp(boolean smp) {
        this.smp = smp;
    }
}
