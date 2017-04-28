package com.gnomon.epsos.service;

import java.io.Serializable;

public class SearchMask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private String domain;
    private String friendlyName;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
