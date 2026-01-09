package com.merkle.oss.magnolia.dictionary.util;

import java.util.Locale;

public class LocaleUtil {
    public String toLocaleString(final Locale locale) {
        return locale.toLanguageTag();
    }

    public Locale fromLocaleString(final String locale) {
        return Locale.forLanguageTag(locale);
    }
}
