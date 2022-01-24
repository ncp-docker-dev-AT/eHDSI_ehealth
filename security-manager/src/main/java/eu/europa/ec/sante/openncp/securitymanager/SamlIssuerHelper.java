package eu.europa.ec.sante.openncp.securitymanager;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;

import javax.xml.namespace.QName;

public class SamlIssuerHelper {

    private SamlIssuerHelper() {
    }

    public static <T> T create(Class<T> cls, QName qname) {
        return (T) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname).buildObject(qname);
    }

    public static Attribute createAttribute(String value, String friendlyName, String nameFormat, String name) {

        Attribute attribute = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setFriendlyName(friendlyName);
        attribute.setName(name);
        attribute.setNameFormat(nameFormat);

        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        var xmlObjectBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
        XSString attrVal = (XSString) xmlObjectBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrVal.setValue(value);
        attribute.getAttributeValues().add(attrVal);
        return attribute;
    }
}
