package tr.com.srdc.epsos.ws.server.xca.impl;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import java.util.List;

public class SlotBuilder {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public static SlotType1 build(String name, String value) {

        var slotType1 = OBJECT_FACTORY.createSlotType1();
        slotType1.setName(name);
        slotType1.setValueList(OBJECT_FACTORY.createValueListType());
        slotType1.getValueList().getValue().add(value);
        return slotType1;
    }

    public static SlotType1 build(String name, List<String> values) {

        var slotType1 = OBJECT_FACTORY.createSlotType1();
        slotType1.setName(name);
        slotType1.setValueList(OBJECT_FACTORY.createValueListType());
        values.forEach(value -> slotType1.getValueList().getValue().add(value));
        return slotType1;
    }
}
