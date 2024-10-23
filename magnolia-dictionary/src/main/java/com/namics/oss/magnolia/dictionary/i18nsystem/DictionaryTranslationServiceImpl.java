package com.namics.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.i18nsystem.I18nText;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;

public class DictionaryTranslationServiceImpl implements TranslationService, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TranslationService wrapper;
    private final SiteManager siteManager;
    private final DefaultMessagesManager defaultMessagesManager;
    private final DictionaryMessageBundlesLoader dictionaryMessageBundlesLoader;

    @Inject
    public DictionaryTranslationServiceImpl(
            final TranslationServiceImpl wrapper,
            final DictionaryMessageBundlesLoader dictionaryMessageBundlesLoader,
            final DefaultMessagesManager defaultMessagesManager,
            final SiteManager siteManager
    ) {
        this.dictionaryMessageBundlesLoader = dictionaryMessageBundlesLoader;
        this.defaultMessagesManager = defaultMessagesManager;
        this.wrapper = wrapper;
        this.siteManager = siteManager;
    }

    @Override
    public String translate(final LocaleProvider localeProvider, final String[] keys) {
        return translate(localeProvider, keys, I18nText.NO_FALLBACK);
    }

    @Override
    public String translate(final LocaleProvider localeProvider, final String[] keys, final String fallback) {
        final Locale locale = localeProvider.getLocale();
        if (locale == null) {
            throw new IllegalArgumentException("Locale can't be null");
        }
        if (ArrayUtils.isEmpty(keys)) {
            throw new IllegalArgumentException("Keys can't be null or empty");
        }
        return lookUpKeyInDictionary(keys, locale).orElseGet(() ->
            // not found in dictionary, translate using Magnolia's default service
            wrapper.translate(localeProvider, keys, fallback)
        );
    }

    @Override
    @Deprecated
    public String translate(final LocaleProvider localeProvider, final String basename, final String[] keys) {
        if (basename != null) {
            LOG.debug("Dictionary ignores explicit basename ('{}') for keys '{}'", basename, Arrays.asList(keys));
        }
        return translate(localeProvider, keys);
    }

    @Override
    @Deprecated
    public void reloadMessageBundles() {
        dictionaryMessageBundlesLoader.reload();
    }

    protected Optional<String> lookUpKeyInDictionary(final String[] keys, final Locale locale) {
        LOG.trace("Looking up in dictionary message bundle with key '{}' and Locale '{}'", Arrays.asList(keys), locale);
        // fallback chain similar to info.magnolia.i18nsystem.TranslationServiceImpl.lookUpKeyUntilFound
        return doGetMessage(keys, locale)
                .or(() -> doGetMessage(keys, new Locale(locale.getLanguage(), locale.getCountry())))
                .or(() -> doGetMessage(keys, new Locale(locale.getLanguage())))
                .or(() -> getSiteFallbackLocale().flatMap(fallbackLocale -> doGetMessage(keys, fallbackLocale)))
                .or(() -> doGetMessage(keys, defaultMessagesManager.getDefaultLocale()));
    }

    protected Optional<String> doGetMessage(final String[] keys, final Locale locale) {
        return Optional
                .ofNullable(dictionaryMessageBundlesLoader.getMessages().get(locale))
                .stream()
                .flatMap(properties ->
                        Arrays.stream(keys)
                                .map(DictionaryUtils::getValidMessageNodeName)
                                .map(properties::getProperty)
                )
                .findFirst();
    }

    private Optional<Locale> getSiteFallbackLocale() {
        return Optional.of(siteManager.getCurrentSite())
                .map(Site::getI18n)
                .map(I18nContentSupport::getFallbackLocale);
    }

    @Override
    public void onEvent(EventIterator events) {
        if (events.getSize() > 0) {
            dictionaryMessageBundlesLoader.reload();
        }
    }
}
