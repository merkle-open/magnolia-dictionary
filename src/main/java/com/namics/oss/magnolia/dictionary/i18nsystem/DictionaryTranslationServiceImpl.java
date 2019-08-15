package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.i18nsystem.module.I18nModule;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.ResourceOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * @author mrauch, Namics AG
 * @since 11.03.2016
 */
@Singleton
public class DictionaryTranslationServiceImpl implements TranslationService, EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(DictionaryTranslationServiceImpl.class);

	private final TranslationService translationService;
	private final ComponentProvider componentProvider;
	private final ResourceOrigin resourceOrigin;

	private DictionaryMessageBundlesLoader dictionaryMessageBundles;

	@Inject
	public DictionaryTranslationServiceImpl(final Provider<I18nModule> i18nModuleProvider,
	                                        final ComponentProvider componentProvider,
	                                        final ResourceOrigin resourceOrigin,
	                                        @Named(SystemEventBus.NAME) EventBus systemEventBus) {
		this.componentProvider = componentProvider;
		this.resourceOrigin = resourceOrigin;
		this.translationService = new TranslationServiceImpl(i18nModuleProvider, componentProvider, resourceOrigin, systemEventBus);
	}

	protected DictionaryMessageBundlesLoader getDictionaryMessageBundles() {
		if (dictionaryMessageBundles == null) {
			dictionaryMessageBundles = componentProvider.newInstance(DictionaryMessageBundlesLoader.class, resourceOrigin);
		}
		return dictionaryMessageBundles;
	}

	@Override
	public String translate(LocaleProvider localeProvider, String[] keys) {
		return translate(localeProvider, null, keys);
	}

	@Override
	public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
		final Locale locale = localeProvider.getLocale();
		if (locale == null) {
			throw new IllegalArgumentException("Locale can't be null");
		}
		if (keys == null || keys.length < 1) {
			throw new IllegalArgumentException("Keys can't be null or empty");
		}

		if (basename != null) {
			LOG.debug("Dictionary ignores explicit basename ({}) for keys {}", basename, Arrays.asList(keys));
		}

		final String message = lookUpKeyInDictionary(keys, locale);
		if (message != null) {
			return message;
		}

		// not found in dictionary, translate using Magnolia's default service
		return translationService.translate(localeProvider, basename, keys);
	}

	protected String lookUpKeyInDictionary(String[] keys, Locale locale) {
		String message;

		LOG.trace("Looking up in dictionary message bundle with key [{}] and Locale [{}]", Arrays.asList(keys), locale);
		message = this.doGetMessage(keys, locale);

		if (message == null) {
			String newLocale = locale.getCountry();
			if (newLocale != null) {
				Locale newLocale1 = new Locale(locale.getLanguage(), newLocale);
				message = this.doGetMessage(keys, newLocale1);
			}
		}

		if (message == null) {
			Locale newLocale2 = new Locale(locale.getLanguage());
			message = this.doGetMessage(keys, newLocale2);
		}

		if (message == null && getSiteI18n() != null) {
			message = this.doGetMessage(keys, getSiteI18n().getFallbackLocale());
		}

		if (message == null) {
			message = this.doGetMessage(keys, this.getFallbackLocale());
		}

		return message;
	}

	protected String doGetMessage(String[] keys, Locale locale) {
		final Properties properties = getDictionaryMessageBundles() != null
				&& getDictionaryMessageBundles().getMessages() != null ? getDictionaryMessageBundles().getMessages().get(locale) : null;
		if (properties != null) {
			for (String key : keys) {
				String validKey = DictionaryUtils.getValidMessageNodeName(key);
				final String message = properties.getProperty(validKey);
				if (message != null) {
					return message;
				}
			}
		}
		return null;
	}

	protected I18nContentSupport getSiteI18n() {
		try {
			if (isSitePresent()) {
				Site currentSite = Components.getComponent(SiteManager.class).getCurrentSite();
				return currentSite.getI18n();
			}
		} catch (RuntimeException e) {
			LOG.debug("Error while getting I18nContentSupport: '{}'", e.getMessage());
			LOG.trace("Error while getting I18nContentSupport", e);
		}
		return null;
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

	protected Locale getFallbackLocale() {
		return MessagesManager.getInstance().getDefaultLocale();
	}

	@Override
	public void reloadMessageBundles() {
		dictionaryMessageBundles = null;
		translationService.reloadMessageBundles();
	}

	@Override
	public void onEvent(EventIterator events) {
		reloadMessageBundles();
	}

	public TranslationService getTranslationService() {
		return this.translationService;
	}
}
