package com.gnomon.epsos.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean
@SessionScoped
public class ConfirmationBean implements Serializable {

    private static final long serialVersionUID = 172996144250283038L;

    private String confirm;
    private String purposeOfUse;
    private String dispensationPinCode;
    private String prescriptionId;

    @ManagedProperty(value = "#{myBean}")
    private MyBean myBean;

    /**
     * A method setting the MyBean property.
     *
     * @param myBean the bean to set.
     */
    public void setMyBean(MyBean myBean) {
        this.myBean = myBean;
    }

    public String getDispensationPinCode() {
        return dispensationPinCode;
    }

    public void setDispensationPinCode(String dispensationPinCode) {
        this.dispensationPinCode = dispensationPinCode;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

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

    public String viewPrescriptions() {
        myBean.setPurposeOfUseForEP(this.getPurposeOfUse());
        return "viewPrescriptions.xhtml?faces-redirect=true&amp;javax.portlet.faces.PortletMode=view&amp;javax.portlet.faces.WindowState=normal";
    }
}
