package com.namics.oss.magnolia.dictionary;

import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryMessageBundlesInstaller;
import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryTranslationServiceImpl;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.ResourceOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.observation.EventListener;

public class DictionaryModule implements ModuleLifecycle {

	private static final Logger LOG = LoggerFactory.getLogger(DictionaryModule.class);

	private EventListener listener;
	private DictionaryMessageBundlesInstaller messagesInstaller;

	@Inject
	public DictionaryModule(final ComponentProvider componentProvider, final ResourceOrigin resourceOrigin) {
		messagesInstaller = componentProvider.newInstance(DictionaryMessageBundlesInstaller.class, resourceOrigin);
	}

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		DictionaryTranslationServiceImpl dictionaryTranslationService = (DictionaryTranslationServiceImpl) Components.getComponentProvider().getComponent(TranslationService.class);
		this.listener = ObservationUtil.instanciateDeferredEventListener(dictionaryTranslationService, 100, 3000);

		try {
			ObservationUtil.registerChangeListener(DictionaryConfiguration.REPOSITORY, "/", listener);
		} catch (Exception e) {
			LOG.warn("Failed to register observation for repository {} due to {}", DictionaryConfiguration.REPOSITORY, e.getMessage());
		}

		messagesInstaller.loadLabelsToDictionary();
	}

	@Override
	public void stop(ModuleLifecycleContext moduleLifecycleContext) {
		try {
			ObservationUtil.unregisterChangeListener(DictionaryConfiguration.REPOSITORY, listener);
		} catch (Exception e) {
			LOG.warn("Failed to unregister observation for repository {} due to {}", DictionaryConfiguration.REPOSITORY, e.getMessage());
		}
	}
}
