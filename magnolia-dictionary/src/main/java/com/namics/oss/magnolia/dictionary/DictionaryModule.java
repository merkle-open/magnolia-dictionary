package com.namics.oss.magnolia.dictionary;

import info.magnolia.cms.util.FilteredEventListener;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.observation.WorkspaceEventListenerRegistration;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;
import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryMessageBundlesInstaller;
import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryMessageBundlesLoader;

public class DictionaryModule implements ModuleLifecycle {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long EVENT_DELAY = 2000;
	private static final long EVENT_DELAY_MAX = 30000;
	private static final String OBSERVATION_PATH = "/";

	private final DictionaryMessageBundlesInstaller messagesInstaller;
    private final DictionaryMessageBundlesLoader dictionaryMessageBundlesLoader;
    private WorkspaceEventListenerRegistration.Handle handle;
	private boolean loadLabelsOnStartup = true;

	@Inject
	public DictionaryModule(
			final DictionaryMessageBundlesInstaller dictionaryMessageBundlesInstaller,
			final DictionaryMessageBundlesLoader dictionaryMessageBundlesLoader
	) {
        this.messagesInstaller = dictionaryMessageBundlesInstaller;
        this.dictionaryMessageBundlesLoader = dictionaryMessageBundlesLoader;
	}

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		try {
			this.handle = WorkspaceEventListenerRegistration
					.observe(DictionaryConfiguration.REPOSITORY, OBSERVATION_PATH, new FilteredEventListener(dictionaryMessageBundlesLoader, FilteredEventListener.JCR_SYSTEM_EXCLUDING_PREDICATE))
					.withSubNodes(true)
					.withDelay(EVENT_DELAY, EVENT_DELAY_MAX)
					.withEventTypesMask(Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED)
					.register();
		} catch (RepositoryException e) {
			LOG.error("Could not register workspace event listener for workspace [{}]", DictionaryConfiguration.REPOSITORY, e);
		}

		if(loadLabelsOnStartup()) {
			LOG.info("Start Dictionary module: Load labels to dictionary");
			Exceptions.wrap().run(messagesInstaller::loadLabelsToDictionary);
		}
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

	public boolean loadLabelsOnStartup() {
		return loadLabelsOnStartup;
	}

	public void setLoadLabelsOnStartup(final boolean loadLabelsOnStartup) {
		this.loadLabelsOnStartup = loadLabelsOnStartup;
	}
}
