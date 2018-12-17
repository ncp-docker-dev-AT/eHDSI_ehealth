package eu.esens.abb.nonrep;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.xacml.ctx.*;
import org.opensaml.xacml.ctx.impl.*;
import org.w3c.dom.Element;

import java.util.List;

public class XACMLRequestCreator {

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new IllegalStateException("Unable to bootstrap OpenSAML!!! Did you endorse the XML libraries?", e);
        }
    }

    private final AttributeTypeImplBuilder atib;
    private final AttributeValueTypeImplBuilder avtib;
    private Element request;

    /**
     * @param messageType
     * @param subjectAttributes
     * @param resourceAttributes
     * @param actionAttributes
     * @param environmentAttributes
     * @throws TOElementException
     */
    public XACMLRequestCreator(MessageType messageType, List<XACMLAttributes> subjectAttributes, List<XACMLAttributes> resourceAttributes,
                               List<XACMLAttributes> actionAttributes, List<XACMLAttributes> environmentAttributes)
            throws TOElementException {

        XMLObjectBuilderFactory bf = XMLObjectProviderRegistrySupport.getBuilderFactory();
        atib = (AttributeTypeImplBuilder) bf.getBuilder(AttributeType.DEFAULT_ELEMENT_NAME);
        avtib = (AttributeValueTypeImplBuilder) bf.getBuilder(AttributeValueType.DEFAULT_ELEMENT_NAME);

        RequestTypeImplBuilder rtb = (RequestTypeImplBuilder) bf.getBuilder(RequestType.DEFAULT_ELEMENT_NAME);
        RequestType requestType = rtb.buildObject();

        SubjectTypeImplBuilder stib = (SubjectTypeImplBuilder) bf.getBuilder(SubjectType.DEFAULT_ELEMENT_NAME);
        SubjectType subject = stib.buildObject();

        ResourceTypeImplBuilder rtib = (ResourceTypeImplBuilder) bf.getBuilder(ResourceType.DEFAULT_ELEMENT_NAME);
        ResourceType resource = rtib.buildObject();

        ActionTypeImplBuilder actib = (ActionTypeImplBuilder) bf.getBuilder(ActionType.DEFAULT_ELEMENT_NAME);
        ActionType action = actib.buildObject();

        EnvironmentTypeImplBuilder etib = (EnvironmentTypeImplBuilder) bf.getBuilder(EnvironmentType.DEFAULT_ELEMENT_NAME);
        EnvironmentType environment = etib.buildObject();

        requestType.getSubjects().add(subject);
        requestType.getResources().add(resource);
        requestType.setAction(action);
        requestType.setEnvironment(environment);

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

        this.request = Utilities.toElement(requestType);
    }

    public Element getRequest() {
        return this.request;
    }
}
