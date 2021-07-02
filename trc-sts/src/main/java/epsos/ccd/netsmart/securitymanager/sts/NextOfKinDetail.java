package epsos.ccd.netsmart.securitymanager.sts;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class NextOfKinDetail {

    private List<String> livingSubjectIds;

    private String givenName;

    private String surname;

    private String gender;

    private Date birthdate;

    private String addressStreet;

    private String addressCity;

    private String addressPostCode;

    private String addressCountry;

    public NextOfKinDetail() {
    }

    public List<String> getLivingSubjectIds() {
        return livingSubjectIds;
    }

    public void setLivingSubjectIds(List<String> livingSubjectIds) {
        this.livingSubjectIds = livingSubjectIds;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressPostCode() {
        return addressPostCode;
    }

    public void setAddressPostCode(String addressPostCode) {
        this.addressPostCode = addressPostCode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("livingSubjectIds", livingSubjectIds)
                .append("givenName", givenName)
                .append("surname", surname)
                .append("gender", gender)
                .append("birthdate", birthdate)
                .append("addressStreet", addressStreet)
                .append("addressCity", addressCity)
                .append("addressPostCode", addressPostCode)
                .append("addressCountry", addressCountry)
                .toString();
    }
}
