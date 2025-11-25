package com.merkle.oss.magnolia.dictionary.util;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class LocaleUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SiteManager siteManager;

    @Inject
    public LocaleUtils(final SiteManager siteManager) {
        this.siteManager = siteManager;
    }

	public Stream<Locale> streamLocalesOfAllSites() {
        try {
            return siteManager.getSites()
                    .stream()
                    .map(Site::getI18n)
                    .map(I18nContentSupport::getLocales)
                    .flatMap(Collection::stream)
                    .distinct();
        } catch (Exception e) {
            LOG.error("Failed to get locales of all sites", e);
            return Stream.empty();
        }
	}

    public String getLocaleString(final Locale locale) {
        return locale.toString();
    }
}
