package com.namics.oss.magnolia.dictionary.util;

import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author mrauch, Namics AG
 * @since 15.03.2016
 */
public class LocaleUtils {

    public static String getLocaleString(Locale locale) {
        return locale.toString();
    }

    public static List<Locale> getLocalesOfAllSiteDefinitions() {
        List<Locale> locales = new ArrayList<>();

        SiteManager manager = Components.getComponent(SiteManager.class);
        for (Site site : manager.getSites()) {
            if (site.getI18n() != null) {
                site.getI18n().getLocales().stream()
                        .filter(locale -> !locales.contains(locale))
                        .forEach(locales::add);
            }
        }

        return locales;
    }
}
