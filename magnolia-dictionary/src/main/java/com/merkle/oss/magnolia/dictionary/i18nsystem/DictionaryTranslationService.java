package com.merkle.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.site.Site;

public interface DictionaryTranslationService extends TranslationService {
    String translate(Site site, LocaleProvider localeProvider, String[] keys);
    String translate(Site site, LocaleProvider localeProvider, String[] keys, String fallback);
}
