package com.merkle.oss.magnolia.dictionary.util;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.site.ConfiguredSite;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class SiteProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String GENERIC_SITE_NAME = "_generic_site";
    private final SiteManager siteManager;
    private final Provider<Site> gernericSiteProvider;

    @Inject
    public SiteProvider(final SiteManager siteManager) {
        this.siteManager = siteManager;
        this.gernericSiteProvider = Lazy.of(() -> {
            final DefaultI18nContentSupport i18n = new DefaultI18nContentSupport();
            i18n.setLocales(streamLocalesOfAllSites().map(locale ->
                    LocaleDefinition.make(locale.getLanguage(), locale.getCountry(), true)
            ).collect(Collectors.toSet()));

            final ConfiguredSite genericSite = new ConfiguredSite();
            genericSite.setName(GENERIC_SITE_NAME);
            genericSite.setI18n(i18n);
            return genericSite;
        })::get;
    }

    public Site getGenericSite() {
        return gernericSiteProvider.get();
    }

    public Optional<Site> getSite(final String siteName) {
        return Optional.ofNullable(siteManager.getSite(siteName));
    }

    public Stream<Site> streamAllSites() {
        return streamAllSites(true);
    }
    public Stream<Site> streamAllSites(final boolean includeGeneric) {
        final Site defaultSite = siteManager.getDefaultSite();
        return Stream.concat(
                Stream.of(getGenericSite()).filter(ignored -> includeGeneric),
                siteManager.getSites().stream().filter(site -> !Objects.equals(defaultSite.getName(), site.getName()))
        );
    }

    private Stream<Locale> streamLocalesOfAllSites() {
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
}
