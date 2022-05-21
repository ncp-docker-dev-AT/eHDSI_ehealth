package tr.com.srdc.epsos.ws.server.xca.impl;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory;
import tr.com.srdc.epsos.util.Constants;

import java.util.UUID;

public class ClassificationBuilder {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public static ClassificationType build(String classificationScheme, String classifiedObject, String nodeRepresentation, String value, String name) {

        var classificationType = build(classificationScheme, classifiedObject, nodeRepresentation);
        classificationType.getSlot().add(SlotBuilder.build("codingScheme", value));

        classificationType.setName(OBJECT_FACTORY.createInternationalStringType());
        classificationType.getName().getLocalizedString().add(OBJECT_FACTORY.createLocalizedStringType());
        classificationType.getName().getLocalizedString().get(0).setValue(name);
        return classificationType;
    }

    public static ClassificationType build(String classificationScheme, String classifiedObject, String nodeRepresentation) {

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        var classificationType = OBJECT_FACTORY.createClassificationType();
        classificationType.setId(uuid);
        classificationType.setNodeRepresentation(nodeRepresentation);
        classificationType.setClassificationScheme(classificationScheme);
        classificationType.setClassifiedObject(classifiedObject);
        return classificationType;
    }
}
