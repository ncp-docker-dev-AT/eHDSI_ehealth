package com.gnomon.epsos.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean
@SessionScoped
public class ConfirmationBean implements Serializable {

    private static final long serialVersionUID = 172996144250283038L;
    private String confirm;
    private String purposeOfUse;

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public String getPurposeOfUse() {
        return purposeOfUse;
    }

    public void setPurposeOfUse(String purposeOfUse) {
        this.purposeOfUse = purposeOfUse;
    }
}
