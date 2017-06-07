package epsos.ccd.gnomon.tsleditor.model;

import java.util.List;
import java.util.Locale;

import org.etsi.uri._02231.v2.InternationalNamesType;
import org.etsi.uri._02231.v2.MultiLangNormStringType;
import org.etsi.uri._02231.v2.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustServiceListUtils {

	private static final Logger LOG = LoggerFactory.getLogger(TrustServiceListUtils.class);

	private TrustServiceListUtils() {
		super();
	}

	/**
	 * Gives back the value according to the given locale.
	 *
	 * @param i18nName
	 * @param locale
	 * @return
	 */
	public static String getValue(InternationalNamesType i18nName, Locale locale) {
		if (null == i18nName) {
			return null;
		}
		List<MultiLangNormStringType> names = i18nName.getName();
		String enValue = null;
		for (MultiLangNormStringType name : names) {
			String lang = name.getLang().toLowerCase();
			if ("en".equals(lang)) {
				enValue = name.getValue();
			}
			if (locale.getLanguage().equals(lang)) {
				return name.getValue();
			}
		}
		if (null != enValue) {
			return enValue;
		}
		return names.get(0).getValue();
	}

	/**
	 * Sets the value according to the given locale.
	 *
	 * @param value
	 * @param locale
	 * @param i18nName
	 */
	public static void setValue(String value, Locale locale, InternationalNamesType i18nName) {
		LOG.debug("set value for locale: " + locale.getLanguage());
		List<MultiLangNormStringType> names = i18nName.getName();
		/*
		 * First try to locate an existing entry for the given locale.
		 */
		MultiLangNormStringType localeName = null;
		for (MultiLangNormStringType name : names) {
			String lang = name.getLang().toLowerCase();
			if (locale.getLanguage().equals(lang)) {
				localeName = name;
				break;
			}
		}
		if (null == localeName) {
			/*
			 * If none was found, create a new one.
			 */
			ObjectFactory objectFactory = new ObjectFactory();
			localeName = objectFactory.createMultiLangNormStringType();
			localeName.setLang(locale.getLanguage());
			names.add(localeName);
		}
		localeName.setValue(value);
	}
}
