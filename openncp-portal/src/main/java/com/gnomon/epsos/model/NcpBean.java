package com.gnomon.epsos.model;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.service.Demographics;
import com.gnomon.epsos.service.EpsosHelperService;
import com.gnomon.epsos.service.SearchMask;
import com.liferay.portal.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@ManagedBean
@SessionScoped
public class NcpBean implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NcpBean.class);
    private static final long serialVersionUID = -3598676527887691187L;

    private String errorUserAssertion;
    private List<Country> countries;
    private String selectedCountry;
    private boolean showDemographics;
    private List<Identifier> identifiers;
    private List<Demographics> demographics;

    public NcpBean() {

        LOGGER.info("Initializing NcpBean ...");
        new MyBean();
        selectedCountry = EpsosHelperService.getConfigProperty("ncp.country");
        identifiers = new ArrayList<>();
        demographics = new ArrayList<>();
        setIdentifiers(selectedCountry);
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    private void setIdentifiers(String selectedCountry) {

        LOGGER.info("Setting identifiers for: '{}'", selectedCountry);
        User user = LiferayUtils.getPortalUser();
        LiferayUtils.storeToSession("user", user);

        identifiers = new ArrayList<>();
        Vector vec = EpsosHelperService.getCountryIdsFromCS(selectedCountry);
        for (Object aVec1 : vec) {
            Identifier id = new Identifier();
            id.setKey(EpsosHelperService.getPortalTranslation(
                    ((SearchMask) aVec1).getLabel(), LiferayUtils.getPortalLanguage()) + "*");
            id.setDomain(((SearchMask) aVec1).getDomain());
            
            if (StringUtils.isBlank(id.getKey()) || StringUtils.equals(id.getKey(), "*")) {
                id.setKey(((SearchMask) aVec1).getLabel() + "*");
            }
            identifiers.add(id);
            LOGGER.info("Identifier: '{}'", id);
        }

        demographics = new ArrayList<>();
        vec = EpsosHelperService.getCountryDemographicsFromCS(this.selectedCountry);
        for (Object aVec : vec) {
            Demographics id = new Demographics();
            if (((Demographics) aVec).getMandatory()) {
                id.setLabel(EpsosHelperService.getPortalTranslation(
                        ((Demographics) aVec).getLabel(), LiferayUtils.getPortalLanguage()) + "*");
            } else {
                id.setLabel(EpsosHelperService.getPortalTranslation(
                        ((Demographics) aVec).getLabel(),
                        LiferayUtils.getPortalLanguage()));
            }
            id.setLength(((Demographics) aVec).getLength());
            id.setKey(((Demographics) aVec).getKey());
            id.setMandatory(((Demographics) aVec).getMandatory());
            id.setType(((Demographics) aVec).getType());
            demographics.add(id);
        }
        showDemographics = !demographics.isEmpty();
        LiferayUtils.storeToSession("selectedCountry", selectedCountry);
    }

    public boolean isShowDemographics() {
        return showDemographics;
    }

    public void setShowDemographics(boolean showDemographics) {
        this.showDemographics = showDemographics;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Demographics> getDemographics() {
        return demographics;
    }

    public void setDemographics(List<Demographics> demographics) {
        this.demographics = demographics;
    }
}
