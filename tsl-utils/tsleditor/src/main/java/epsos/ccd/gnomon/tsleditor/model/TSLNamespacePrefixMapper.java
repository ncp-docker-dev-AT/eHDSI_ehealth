package epsos.ccd.gnomon.tsleditor.model;

import java.util.HashMap;
import java.util.Map;


import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSLNamespacePrefixMapper extends NamespacePrefixMapper {

    private static final Logger LOG = LoggerFactory.getLogger
            (TSLNamespacePrefixMapper.class);

	private static final Map<String, String> prefixes = new HashMap<String, String>();

	static {
		prefixes.put("http://uri.etsi.org/02231/v2#", "tsl");
		prefixes.put("http://www.w3.org/2000/09/xmldsig#", "ds");
		prefixes
				.put(
						"http://uri.etsi.org/TrstSvc/SvcInfoExt/eSigDir-1999-93-EC-TrustedList/#",
						"ecc");
		prefixes.put("http://uri.etsi.org/01903/v1.3.2#", "xades");
		prefixes.put("http://uri.etsi.org/02231/v2/additionaltypes#", "tslx");
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion,
			boolean requirePrefix) {
        LOG.debug("get preferred prefix: '{}'", namespaceUri);
        LOG.debug("suggestion: '{}", suggestion);
        String prefix = prefixes.get(namespaceUri);
		if (null != prefix) {
			return prefix;
		}
		return suggestion;
	}
}
