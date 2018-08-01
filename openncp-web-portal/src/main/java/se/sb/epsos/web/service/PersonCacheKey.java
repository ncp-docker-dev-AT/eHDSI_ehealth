package se.sb.epsos.web.service;

import se.sb.epsos.web.util.EpsosStringUtils;

import java.io.Serializable;

/**
 * @author andreas
 */
public class PersonCacheKey implements Serializable {

    private static final long serialVersionUID = 7639120662338895244L;
    private String sessionId;
    private String personId;

    public PersonCacheKey(String sessionId, String personId) {
        this.sessionId = sessionId;
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "{" + this.sessionId + ", " + this.personId + "}";
    }

    @Override
    public int hashCode() {
        return (sessionId + personId).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PersonCacheKey)) return false;
        PersonCacheKey key = (PersonCacheKey) o;
        boolean test = EpsosStringUtils.nullSafeCompare(this.sessionId, key.getSessionId());
        test = test && EpsosStringUtils.nullSafeCompare(this.personId, key.getPersonId());
        return test;
    }
}
