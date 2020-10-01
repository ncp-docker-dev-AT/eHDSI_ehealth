package com.gnomon.epsos.model;

import java.io.Serializable;

public class Identifier implements Serializable {

    private static final long serialVersionUID = 7915098873159217799L;
    private String key;
    private String domain;
    private String userValue;
    private String friendlyName;
    private boolean required;

    public Identifier() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserValue() {
        return userValue;
    }

    public void setUserValue(String userValue) {
        this.userValue = userValue;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
