package com.namics.oss.magnolia.dictionary;

import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryMessageBundlesInstaller;
import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryTranslationServiceImpl;
import info.magnolia.cms.util.FilteredEventListener;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.observation.WorkspaceEventListenerRegistration;
import info.magnolia.resourceloader.ResourceOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import java.lang.invoke.MethodHandles;

public class DictionaryModule implements ModuleLifecycle {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long EVENT_DELAY = 2000;
	private static final long EVENT_DELAY_MAX = 30000;
	private static final String OBSERVATION_PATH = "/";

	private DictionaryMessageBundlesInstaller messagesInstaller;
	private WorkspaceEventListenerRegistration.Handle handle;

	@Inject
	public DictionaryModule(final ComponentProvider componentProvider, final ResourceOrigin resourceOrigin) {
		messagesInstaller = componentProvider.newInstance(DictionaryMessageBundlesInstaller.class, resourceOrigin);
	}

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		try {
			DictionaryTranslationServiceImpl translationService = (DictionaryTranslationServiceImpl) Components.getComponent(TranslationService.class);
			this.handle = WorkspaceEventListenerRegistration
					.observe(DictionaryConfiguration.REPOSITORY, OBSERVATION_PATH, new FilteredEventListener(translationService, FilteredEventListener.JCR_SYSTEM_EXCLUDING_PREDICATE))
					.withSubNodes(true)
					.withDelay(EVENT_DELAY, EVENT_DELAY_MAX)
					.withEventTypesMask(Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED)
					.register();
		} catch (RepositoryException e) {
			LOG.error("Could not register workspace event listener for workspace [{}]", DictionaryConfiguration.REPOSITORY, e);
		}

		LOG.info("Start Dictionary module: Load labels to dictionary");
		messagesInstaller.loadLabelsToDictionary();
	}

	@Override
	public void stop(ModuleLifecycleContext moduleLifecycleContext) {
		if (handle != null) {
			try {
				handle.unregister();
			} catch (RepositoryException e) {
				LOG.error("Could not unregister workspace event listener for workspace [{}]", DictionaryConfiguration.REPOSITORY, e);
			}
		}
	}
}
