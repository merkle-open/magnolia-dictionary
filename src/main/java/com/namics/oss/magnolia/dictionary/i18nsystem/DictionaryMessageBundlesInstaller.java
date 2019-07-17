package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.util.FileResourceCollectorVisitor;
import info.magnolia.resourceloader.util.Functions;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

public class DictionaryMessageBundlesInstaller {

	private static final Logger LOG = LoggerFactory.getLogger(DictionaryMessageBundlesInstaller.class);
	public static final String LAST_LOADED_TIME = "lastLoadedTime";

	private static final Predicate<Resource> DIRECTORY_PREDICATE = Functions.pathStartsWith("/");
	private static final Predicate<Resource> RESOURCE_PREDICATE = Functions.pathMatches("(/*/i18n/.*dictionary-messages.*\\.properties|/mgnl-i18n/.*dictionary-messages.*\\.properties)");

	private static final String MGNL_DICTIONARY_CONFIG_NODE_TYPE = "mgnl:dictionaryConfig";

	private final Properties messages = new Properties();

	@Inject
	public DictionaryMessageBundlesInstaller(ResourceOrigin resourceOrigin) {
		loadMessages(resourceOrigin, newVisitor());
	}

	private FileResourceCollectorVisitor newVisitor() {
		return FileResourceCollectorVisitor.on(DIRECTORY_PREDICATE, RESOURCE_PREDICATE);
	}

	private void loadMessages(ResourceOrigin resourceOrigin, FileResourceCollectorVisitor visitor) {
		resourceOrigin.traverseWith(visitor);
		final Collection<Resource> collected = visitor.getCollectedResources();
		for (Resource propertyFile : collected) {
			loadResources(propertyFile);
		}
	}

	private void loadResources(Resource propertiesFile) {
		try (InputStream in = propertiesFile.openStream()) {
			LOG.debug("Loading properties file at [{}]...", propertiesFile);

			final Reader inStream = new InputStreamReader(new BOMInputStream(in), "UTF-8");
			final Properties properties = new Properties();
			properties.load(inStream);

			messages.putAll(properties);
		} catch (IOException e) {
			LOG.warn("An IO error occurred while trying to read properties file at [{}]", propertiesFile, e);
		}
	}

	public void loadLabelsToDictionary() {
		Node dictionaryRoot = NodeUtil.getNodeByPathOrNull(DictionaryConfiguration.REPOSITORY, "/");
		try {
			NodeUtil.overwriteOrCreateNode(dictionaryRoot, LAST_LOADED_TIME, MGNL_DICTIONARY_CONFIG_NODE_TYPE); // lastLoadedTime = node.lastModifiedTime
		} catch (RepositoryException e) {
			LOG.error("Could not set time when labels were last loaded. Manually check labels which should get removed.", e);
		}
		for (Map.Entry<Object, Object> message : messages.entrySet()) {

			String messageName = message.getKey().toString();
			String messageNodeName = DictionaryUtils.getValidMessageNodeName(messageName);
			if (!messageName.equals(messageNodeName)) {
				LOG.info("Label name changed from {} to {}", message.getKey().toString(), messageNodeName);
			}

			Node node = NodeUtil.getOrCreateNode(dictionaryRoot, messageNodeName, DictionaryConfiguration.NODE_TYPE);
			try {
				PropertyUtil.setProperty(node, "name", message.getKey().toString());
				PropertyUtil.setProperty(node, "value", message.getValue().toString());
				node.getSession().save();
			} catch (RepositoryException e) {
				LOG.debug("Could not create label: {}", e.toString()); // use toString so that stacktrace does not get printed
			}
		}
	}

	public Properties getMessages() {
		return messages;
	}

}
