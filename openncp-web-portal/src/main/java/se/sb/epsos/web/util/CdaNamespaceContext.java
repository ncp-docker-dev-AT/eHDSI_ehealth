package se.sb.epsos.web.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import com.google.common.collect.ImmutableMap;

public class CdaNamespaceContext implements NamespaceContext {
	private Map<String, String> ns = ImmutableMap.of( //
			"hl7", "urn:hl7-org:v3", //
			"epsos", "urn:epsos-org:ep:medication", //
			"xsi", "http://www.w3.org/2001/XMLSchema-instance" //
	);

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		ArrayList<String> list = new ArrayList<String>();
		for (String prefix : ns.keySet()) {
			if (ns.get(prefix).equals(namespaceURI)) {
				list.add(prefix);
			}
		}
		return list.iterator();
	}

	@Override
	public String getPrefix(String namespaceURI) {
		for (String prefix : ns.keySet()) {
			if (ns.get(prefix).equals(namespaceURI)) {
				return prefix;
			}
		}
		return null;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return ns.get(prefix);
	}
}
