package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.i18nsystem.I18nText;
import info.magnolia.i18nsystem.module.I18nModule;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.ResourceOrigin;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * @author mrauch, Namics AG
 * @since 11.03.2016
 */
@Singleton
public class DictionaryTranslationServiceImpl implements TranslationService, EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final TranslationService translationService;
	private final ComponentProvider componentProvider;
	private final ResourceOrigin resourceOrigin;
	private final DefaultMessagesManager defaultMessagesManager;

	private DictionaryMessageBundlesLoader dictionaryMessageBundles;

	@Inject
	public DictionaryTranslationServiceImpl(final Provider<I18nModule> i18nModuleProvider,
	                                        final Provider<DefaultMessageBundlesLoader> defaultMessageBundlesLoaderProvider,
	                                        final ComponentProvider componentProvider,
	                                        final ResourceOrigin resourceOrigin,
	                                        final DefaultMessagesManager defaultMessagesManager) {
		this.componentProvider = componentProvider;
		this.resourceOrigin = resourceOrigin;
		this.defaultMessagesManager = defaultMessagesManager;
		this.translationService = new TranslationServiceImpl(i18nModuleProvider, defaultMessageBundlesLoaderProvider);
	}

	protected DictionaryMessageBundlesLoader getDictionaryMessageBundles() {
		if (dictionaryMessageBundles == null) {
			dictionaryMessageBundles = componentProvider.newInstance(DictionaryMessageBundlesLoader.class, resourceOrigin);
		}
		return dictionaryMessageBundles;
	}

	@Override
	public String translate(LocaleProvider localeProvider, String[] keys) {
		return this.translate(localeProvider, keys, I18nText.NO_FALLBACK);
	}

	@Override
	public String translate(LocaleProvider localeProvider, String[] keys, String fallback) {
		final Locale locale = localeProvider.getLocale();
		if (locale == null) {
			throw new IllegalArgumentException("Locale can't be null");
		}
		if (ArrayUtils.isEmpty(keys)) {
			throw new IllegalArgumentException("Keys can't be null or empty");
		}

		final String message = lookUpKeyInDictionary(keys, locale);
		if (message != null) {
			return message;
		}
		// not found in dictionary, translate using Magnolia's default service
		return translationService.translate(localeProvider, keys, fallback);
	}

	@Override
	@Deprecated
	public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
		if (basename != null) {
			LOG.debug("Dictionary ignores explicit basename ('{}') for keys '{}'", basename, Arrays.asList(keys));
		}
		return translate(localeProvider, keys);
	}

	@Override
	@Deprecated
	public void reloadMessageBundles() {
		resetMessageBundles();
	}

	protected String lookUpKeyInDictionary(String[] keys, Locale locale) {
		LOG.trace("Looking up in dictionary message bundle with key '{}' and Locale '{}'", Arrays.asList(keys), locale);
		// fallback chain similar to info.magnolia.i18nsystem.TranslationServiceImpl.lookUpKeyUntilFound
		return doGetMessage(keys, locale)
				.or(() -> doGetMessage(keys, new Locale(locale.getLanguage(), locale.getCountry())))
				.or(() -> doGetMessage(keys, new Locale(locale.getLanguage())))
				.or(() -> doGetMessage(keys, getSiteFallbackLocale()))
				.or(() -> doGetMessage(keys, getFallbackLocale()))
				.orElse(null);
	}

	protected Optional<String> doGetMessage(String[] keys, Locale locale) {
		Optional<Properties> properties = Optional.ofNullable(getDictionaryMessageBundles())
				.map(DictionaryMessageBundlesLoader::getMessages)
				.map(mapper -> mapper.get(locale));

		return Arrays.stream(keys)
				.map(DictionaryUtils::getValidMessageNodeName)
				.map(nodeName -> properties.map(props -> props.getProperty(nodeName)).orElse(null))
				.filter(Objects::nonNull)
				.findFirst();
	}

	private Locale getSiteFallbackLocale() {
		return getSiteI18n()
				.map(I18nContentSupport::getFallbackLocale)
				.orElse(null);
	}

	private Optional<I18nContentSupport> getSiteI18n() {
		try {
			if (isSitePresent()) {
				Site currentSite = Components.getComponent(SiteManager.class).getCurrentSite();
				return Optional.of(currentSite.getI18n());
			}
		} catch (RuntimeException e) {
			LOG.debug("Error while getting I18nContentSupport: '{}'", e.getMessage());
			LOG.trace("Error while getting I18nContentSupport", e);
		}
		return Optional.empty();
	}

	private boolean isSitePresent() {
		// If a translation is requested before the site is
		// set in the aggregationState, the SiteManager will
		// log a warning (see info.magnolia.module.site.DefaultSiteManager.getCurrentSite).
		// This happens, if the translation is requested in a
		// filter which is located before the info.magnolia.multisite.filters.MultiSiteFilter.
		// This is the case in Magnolia 5.7 info.magnolia.personalization.visitor.VisitorDetectorFilter.
		if (MgnlContext.isWebContext()) {
			return Optional.ofNullable(MgnlContext.getAggregationState())
					.filter(state -> state instanceof ExtendedAggregationState)
					.map(state -> ((ExtendedAggregationState) state).getSite())
					.isPresent();
		}
		return Boolean.FALSE;
	}

	private Locale getFallbackLocale() {
		return defaultMessagesManager.getDefaultLocale();
	}

	private void resetMessageBundles() {
		dictionaryMessageBundles = null;
	}

	@Override
	public void onEvent(EventIterator events) {
		if (events.getSize() > 0) {
			resetMessageBundles();
		}
	}

	public TranslationService getTranslationService() {
		return this.translationService;
	}
}
