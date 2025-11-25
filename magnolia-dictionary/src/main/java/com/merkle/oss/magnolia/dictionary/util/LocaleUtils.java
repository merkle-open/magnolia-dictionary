package com.merkle.oss.magnolia.dictionary.util;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mrauch, Namics AG
 * @since 15.03.2016
 */
public class LocaleUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static String getLocaleString(Locale locale) {
		return locale.toString();
	}

	public static List<Locale> getLocalesOfAllSiteDefinitions() {
        try {
            final SiteManager manager = Components.getComponent(SiteManager.class);
            return manager.getSites()
                    .stream()
                    .map(Site::getI18n)
                    .map(I18nContentSupport::getLocales)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Failed to get locales of all sites", e);
            return Collections.emptyList();
        }
	}
}
