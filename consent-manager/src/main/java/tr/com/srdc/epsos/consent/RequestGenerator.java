package tr.com.srdc.epsos.consent;

import urn.oasis.names.tc.xacml3.AttributeType;
import urn.oasis.names.tc.xacml3.AttributeValueType;
import urn.oasis.names.tc.xacml3.AttributesType;
import urn.oasis.names.tc.xacml3.RequestType;

public class RequestGenerator {

    private RequestGenerator() {
    }

    public static RequestType createRequest(String patientId, String countryCode) {
        RequestType request = new RequestType();

        AttributesType attributesType = new AttributesType();
        request.getAttributes().add(attributesType);
        attributesType.setCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");

        AttributeType attribute = new AttributeType();
        attributesType.getAttribute().add(attribute);
        attribute.setAttributeId("patient-id");

        AttributeValueType attributeValue = new AttributeValueType();
        attribute.getAttributeValue().add(attributeValue);
        attributeValue.setDataType("http://www.w3.org/2001/XMLSchema#string");
        attributeValue.getContent().add(patientId);

        AttributeType attribute2 = new AttributeType();
        attributesType.getAttribute().add(attribute2);
        attribute2.setAttributeId("country-code");

        AttributeValueType attributeValue2 = new AttributeValueType();
        attribute2.getAttributeValue().add(attributeValue2);
        attributeValue2.setDataType("http://www.w3.org/2001/XMLSchema#string");
        attributeValue2.getContent().add(countryCode);

        return request;
    }
}
