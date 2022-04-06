package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import com.namics.oss.magnolia.dictionary.util.Lazy;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import com.namics.oss.magnolia.dictionary.util.predicates.NodeNameFilteringPredicate;
import com.namics.oss.magnolia.dictionary.util.predicates.SystemNodeFilteringPredicate;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DictionaryMessageBundlesInstaller {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String LAST_LOADED_TIME = "lastLoadedTime";

	private final Lazy<Properties> messages;

	@Inject
	public DictionaryMessageBundlesInstaller(
			final I18nResourcesProvider i18nResourcesProvider,
			final ResourceOrigin resourceOrigin) {
		this.messages = Lazy.of(() ->
				loadProperties(i18nResourcesProvider.getI18nResources(resourceOrigin))
		);
	}

	private Properties loadProperties(final Collection<Resource> resources) {
		final Properties properties = new Properties();
		resources.stream().map(this::loadProperties).forEach(properties::putAll);
		return properties;
	}

	private Properties loadProperties(final Resource resource) {
		final Properties properties = new Properties();
		try (InputStream in = resource.openStream()) {
			LOG.debug("Loading properties from '{}'", resource);
			final Reader inStream = new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8);
			properties.load(inStream);
		} catch (Exception e) {
			LOG.error("Failed to read properties from '{}', skipping properties file...", resource, e);
		}
		return properties;
	}

	public Properties getMessages() {
		return messages.get();
	}

	public void loadLabelsToDictionary() {
		Node dictionaryRoot = NodeUtil.getNodeByPathOrNull(DictionaryConfiguration.REPOSITORY, "/");
		setLastLoadedTime(dictionaryRoot);

		List<String> notExpired = new ArrayList<>();

		for (Map.Entry<Object, Object> message : getMessages().entrySet()) {
			String messageValue = message.getValue().toString();
			String messageName = message.getKey().toString();
			String messageNodeName = DictionaryUtils.getValidMessageNodeName(messageName);
			if (!messageName.equals(messageNodeName)) {
				LOG.info("Label name changed from '{}' to '{}'", messageName, messageNodeName);
			}
			// collect list of messages/labels present in the property files
			notExpired.add(messageNodeName);

			Node labelNode = NodeUtil.getOrCreateNode(dictionaryRoot, messageNodeName, DictionaryConfiguration.NODE_TYPE);
			try {
				if (propertyNeedsUpdate(labelNode, messageValue)) {
					PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.NAME, messageName);
					PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.VALUE, messageValue);
					PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.EXPIRED, null);
					labelNode.getSession().save();
				}
			} catch (RepositoryException e) {
				LOG.warn("Could not create label: '{}'", messageName, e);
			}
		}

		try {
			var expiredNodesPredicate = Predicates.and(new NodeNameFilteringPredicate(notExpired), new SystemNodeFilteringPredicate());
			Iterable<Node> expiredNodes = NodeUtil.getNodes(dictionaryRoot, expiredNodesPredicate);
			for (Node expiredNode : expiredNodes) {
				PropertyUtil.setProperty(expiredNode, DictionaryConfiguration.Prop.EXPIRED, true);
				expiredNode.getSession().save();
			}
		} catch (RepositoryException e) {
			LOG.warn("Could not mark expired nodes", e);
		}
	}

	private boolean propertyNeedsUpdate(Node labelNode, String newValue) {
		String currentValue = PropertyUtil.getString(labelNode, DictionaryConfiguration.Prop.VALUE);
		boolean wasExpired = PropertyUtil.getBoolean(labelNode, DictionaryConfiguration.Prop.EXPIRED, false);
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
}
