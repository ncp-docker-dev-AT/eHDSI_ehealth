package tr.com.srdc.epsos.util.xpath;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

public class NamespaceContextImpl implements NamespaceContext {

    private Map namespaceBindings = null;

    public NamespaceContextImpl(Map namespaceBindings) {
        if (namespaceBindings != null) {
            this.namespaceBindings = namespaceBindings;
        } else
            namespaceBindings = new HashMap();
    }

    @Override
    public String getNamespaceURI(String prefix) {

        return (String) namespaceBindings.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {

        if (namespaceBindings.containsValue(namespaceURI)) {
            Iterator itrKeys = namespaceBindings.keySet().iterator();
            for (Object o : namespaceBindings.values()) {
                if (o.equals(namespaceURI)) {
                    return (String) itrKeys.next();
                }
            }
        }
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {

        List<String> result = new ArrayList<>();
        if (namespaceBindings.containsValue(namespaceURI)) {
            Iterator itrKeys = namespaceBindings.keySet().iterator();
            for (Object o : namespaceBindings.values()) {
                if (o.equals(namespaceURI)) {
                    result.add((String) itrKeys.next());
                }
            }
        }
        return result.iterator();
    }
}
