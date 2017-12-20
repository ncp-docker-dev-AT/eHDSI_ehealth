package eu.esens.abb.nonrep;

import org.opensaml.xacml.ctx.*;
import org.opensaml.xacml.ctx.impl.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.w3c.dom.Element;

import java.util.List;

public class XACMLRequestCreator {

    private static final XMLObjectBuilderFactory bf = org.opensaml.xml.Configuration.getBuilderFactory();

    static {
        try {
            org.opensaml.DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Unable to bootstrap OpenSAML!!! Did you endorse the XML libraries?", e);
        }
    }

    private final AttributeTypeImplBuilder atib;
    private final AttributeValueTypeImplBuilder avtib;
    private Element request;

    public XACMLRequestCreator(MessageType messageType, List<XACMLAttributes> subjectAttributes, List<XACMLAttributes> resourceAttributes,
                               List<XACMLAttributes> actionAttributes, List<XACMLAttributes> environmentAttributes)
            throws TOElementException {

        atib = (AttributeTypeImplBuilder) bf.getBuilder(AttributeType.DEFAULT_ELEMENT_NAME);
        avtib = (AttributeValueTypeImplBuilder) bf.getBuilder(AttributeValueType.DEFAULT_ELEMENT_NAME);

        RequestTypeImplBuilder rtb = (RequestTypeImplBuilder) bf.getBuilder(RequestType.DEFAULT_ELEMENT_NAME);
        RequestType request = rtb.buildObject();

        SubjectTypeImplBuilder stib = (SubjectTypeImplBuilder) bf.getBuilder(SubjectType.DEFAULT_ELEMENT_NAME);
        SubjectType subject = stib.buildObject();

        ResourceTypeImplBuilder rtib = (ResourceTypeImplBuilder) bf.getBuilder(ResourceType.DEFAULT_ELEMENT_NAME);
        ResourceType resource = rtib.buildObject();

        ActionTypeImplBuilder actib = (ActionTypeImplBuilder) bf.getBuilder(ActionType.DEFAULT_ELEMENT_NAME);
        ActionType action = actib.buildObject();

        EnvironmentTypeImplBuilder etib = (EnvironmentTypeImplBuilder) bf.getBuilder(EnvironmentType.DEFAULT_ELEMENT_NAME);
        EnvironmentType environment = etib.buildObject();

        request.getSubjects().add(subject);
        request.getResources().add(resource);
        request.setAction(action);
        request.setEnvironment(environment);

        if (subjectAttributes != null) {

            for (XACMLAttributes attributeItem : subjectAttributes) {
                AttributeType attribute = atib.buildObject();
                attribute.setAttributeID(attributeItem.getIdentifier().toASCIIString());
                attribute.setDataType(attributeItem.getDataType().toASCIIString());

                AttributeValueType attributeValue = avtib.buildObject();
                attributeValue.setValue(attributeItem.getValue());
                attribute.getAttributeValues().add(attributeValue);
                subject.getAttributes().add(attribute);

            }
        }
        if (resourceAttributes != null) {

            for (XACMLAttributes attributeItem : resourceAttributes) {
                AttributeType attribute = atib.buildObject();
                attribute.setAttributeID(attributeItem.getIdentifier().toASCIIString());
                attribute.setDataType(attributeItem.getDataType().toASCIIString());

                AttributeValueType attributeValue = avtib.buildObject();
                attributeValue.setValue(attributeItem.getValue());
                attribute.getAttributeValues().add(attributeValue);
                resource.getAttributes().add(attribute);
            }
        }

        if (actionAttributes != null) {

            for (XACMLAttributes attributeItem : actionAttributes) {
                AttributeType attribute = atib.buildObject();
                attribute.setAttributeID(attributeItem.getIdentifier().toASCIIString());
                attribute.setDataType(attributeItem.getDataType().toASCIIString());

                AttributeValueType attributeValue = avtib.buildObject();
                attributeValue.setValue(attributeItem.getValue());
                attribute.getAttributeValues().add(attributeValue);
                action.getAttributes().add(attribute);
            }
        }

        if (environmentAttributes != null) {

            for (XACMLAttributes attributeItem : environmentAttributes) {
                AttributeType attribute = atib.buildObject();
                attribute.setAttributeID(attributeItem.getIdentifier().toASCIIString());
                attribute.setDataType(attributeItem.getDataType().toASCIIString());

                AttributeValueType attributeValue = avtib.buildObject();
                attributeValue.setValue(attributeItem.getValue());
                attribute.getAttributeValues().add(attributeValue);
                environment.getAttributes().add(attribute);
            }
        }

        this.request = Utilities.toElement(request);
    }

    public Element getRequest() {
        return this.request;
    }
}
