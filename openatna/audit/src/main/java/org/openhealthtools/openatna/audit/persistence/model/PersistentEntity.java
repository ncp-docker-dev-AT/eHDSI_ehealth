package org.openhealthtools.openatna.audit.persistence.model;

import java.io.Serializable;

/**
 * Force entities to implement these methods and be serializable
 *
 * @author Andrew Harrison
 * @version $Revision:$
 */
public abstract class PersistentEntity implements Serializable {

    public abstract Long getId();

    public abstract int hashCode();

    public abstract boolean equals(Object other);

    public abstract String toString();
}
