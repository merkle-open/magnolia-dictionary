package com.merkle.oss.magnolia.dictionary.i18nsystem;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.NodeUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.dictionary.util.predicates.LabelNameFilteringPredicate;
import com.merkle.oss.magnolia.dictionary.util.predicates.SystemNodeFilteringPredicate;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
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
    private final Label.Persistor labelPersistor;

    @Inject
	public DictionaryMessageBundlesInstaller(
			final I18nResourcesProvider i18nResourcesProvider,
			final ResourceOrigin resourceOrigin,
			final Provider<SystemContext> systemContextProvider,
			final Label.Persistor labelPersistor
	) {
        this.i18nResourcesProvider = i18nResourcesProvider;
        this.resourceOrigin = resourceOrigin;
		this.systemContextProvider = systemContextProvider;
        this.labelPersistor = labelPersistor;
    }

	public void loadLabelsToDictionary() throws RepositoryException {
		final Session session = systemContextProvider.get().getJCRSession(DictionaryConfiguration.REPOSITORY);
		final Node dictionaryRoot = session.getRootNode();
		setLastLoadedTime(dictionaryRoot);

		final Set<String> notExpired = new HashSet<>();

		streamPropertyEntries().forEach(message -> {
			final String messageValue = message.getValue().toString();
			final String messageName = message.getKey().toString();
			labelPersistor.persist(dictionaryRoot, new Label(messageName, SiteProvider.GENERIC_SITE_NAME, messageValue, Collections.emptyMap()), true);
			// collect list of messages/labels present in the property files
			notExpired.add(messageName);
		});

		markExpiredProperties(dictionaryRoot, notExpired);
		session.save();
	}

	private void markExpiredProperties(final Node dictionaryRoot, final Set<String> notExpired) {
		try {
			final Predicate expiredNodesPredicate = Predicates.and(new LabelNameFilteringPredicate(notExpired), new SystemNodeFilteringPredicate());
			final Iterable<Node> expiredNodes = NodeUtil.getNodes(dictionaryRoot, expiredNodesPredicate);
			for (Node expiredNode : expiredNodes) {
				PropertyUtil.setProperty(expiredNode, DictionaryConfiguration.Prop.EXPIRED, true);
			}
		} catch (RepositoryException e) {
			LOG.warn("Could not mark expired nodes", e);
		}
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
