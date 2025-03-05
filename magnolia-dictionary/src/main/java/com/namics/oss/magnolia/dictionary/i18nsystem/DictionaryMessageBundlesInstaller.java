package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import com.namics.oss.magnolia.dictionary.util.predicates.NodeNameFilteringPredicate;
import com.namics.oss.magnolia.dictionary.util.predicates.SystemNodeFilteringPredicate;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class DictionaryMessageBundlesInstaller {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String LAST_LOADED_TIME = "lastLoadedTime";

    private final I18nResourcesProvider i18nResourcesProvider;
    private final ResourceOrigin<?> resourceOrigin;
	private final Provider<SystemContext> systemContextProvider;

	@Inject
	public DictionaryMessageBundlesInstaller(
			final I18nResourcesProvider i18nResourcesProvider,
			final ResourceOrigin resourceOrigin,
			final Provider<SystemContext> systemContextProvider
	) {
        this.i18nResourcesProvider = i18nResourcesProvider;
        this.resourceOrigin = resourceOrigin;
		this.systemContextProvider = systemContextProvider;
	}

	public void loadLabelsToDictionary() throws RepositoryException {
		final Session session = systemContextProvider.get().getJCRSession(DictionaryConfiguration.REPOSITORY);
		final Node dictionaryRoot = session.getRootNode();
		setLastLoadedTime(dictionaryRoot);

		final Set<String> notExpired = new HashSet<>();

		streamPropertyEntries().forEach(message -> {
			final String messageValue = message.getValue().toString();
			final String messageName = message.getKey().toString();
			final String messageNodeName = DictionaryUtils.getValidMessageNodeName(messageName);
			if (!messageName.equals(messageNodeName)) {
				LOG.info("Label name changed from '{}' to '{}'", messageName, messageNodeName);
			}
			// collect list of messages/labels present in the property files
			notExpired.add(messageNodeName);
			createProperty(dictionaryRoot, messageNodeName, messageValue, messageName);
		});

		markExpiredProperties(dictionaryRoot, notExpired);
		session.save();
	}

	private void markExpiredProperties(final Node dictionaryRoot, final Set<String> notExpired) {
		try {
			final Predicate expiredNodesPredicate = Predicates.and(new NodeNameFilteringPredicate(notExpired), new SystemNodeFilteringPredicate());
			final Iterable<Node> expiredNodes = NodeUtil.getNodes(dictionaryRoot, expiredNodesPredicate);
			for (Node expiredNode : expiredNodes) {
				PropertyUtil.setProperty(expiredNode, DictionaryConfiguration.Prop.EXPIRED, true);
			}
		} catch (RepositoryException e) {
			LOG.warn("Could not mark expired nodes", e);
		}
	}

	private void createProperty(final Node dictionaryRoot, final String messageNodeName, final String messageValue, final String messageName) {
		final Node labelNode = NodeUtil.getOrCreateNode(dictionaryRoot, messageNodeName, DictionaryConfiguration.NODE_TYPE);
		try {
			if (propertyNeedsUpdate(labelNode, messageValue)) {
				PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.NAME, messageName);
				PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.VALUE, messageValue);
				PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.EXPIRED, null);
			}
		} catch (RepositoryException e) {
			LOG.warn("Could not create label: '{}'", messageName, e);
		}
	}

	private boolean propertyNeedsUpdate(Node labelNode, String newValue) {
		final String currentValue = PropertyUtil.getString(labelNode, DictionaryConfiguration.Prop.VALUE);
		final boolean wasExpired = PropertyUtil.getBoolean(labelNode, DictionaryConfiguration.Prop.EXPIRED, false);
		return wasExpired || !StringUtils.equals(currentValue, newValue);
	}

	private void setLastLoadedTime(Node dictionaryRoot) {
		try {
			// lastLoadedTime = node.lastModifiedTime
			NodeUtil.overwriteOrCreateNode(dictionaryRoot, LAST_LOADED_TIME, DictionaryConfiguration.DICTIONARY_CONFIG_NODE_TYPE);
		} catch (RepositoryException e) {
			LOG.error("Could not set time when labels were last loaded. Manually check labels which should get removed.", e);
		}
	}

	private Stream<Map.Entry<Object, Object>> streamPropertyEntries() {
		final Collection<Resource> resources = i18nResourcesProvider.getI18nResources(resourceOrigin);
		return resources.stream()
				.map(this::loadProperties)
				.flatMap(properties ->
						properties.entrySet().stream()
				);
	}

	private Properties loadProperties(final Resource resource) {
		final Properties properties = new Properties();
		try (InputStream in = resource.openStream()) {
			LOG.debug("Loading properties from '{}'", resource);
			final Reader inStream = new InputStreamReader(BOMInputStream.builder().setInputStream(in).get(), StandardCharsets.UTF_8);
			properties.load(inStream);
		} catch (Exception e) {
			LOG.error("Failed to read properties from '{}', skipping properties file...", resource, e);
		}
		return properties;
	}
}
