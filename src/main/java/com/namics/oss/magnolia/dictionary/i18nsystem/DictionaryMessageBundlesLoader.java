package com.namics.oss.magnolia.dictionary.i18nsystem;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.LocaleUtils;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author mrauch, Namics AG
 * @since 11.03.2016
 */
public class DictionaryMessageBundlesLoader {

	private static final Logger LOG = LoggerFactory.getLogger(DictionaryMessageBundlesLoader.class);

	private final Map<Locale, Properties> messages = new HashMap<Locale, Properties>();

	public DictionaryMessageBundlesLoader() {
		loadMessagesInSystemContext();
	}

	protected void loadMessagesInSystemContext() {
		try {
			MgnlContext.doInSystemContext(new MgnlContext.RepositoryOp() {
				@Override
				public void doExec() throws RepositoryException {
					loadMessages();
				}
			});
		} catch (RepositoryException e) {
			LOG.warn("An error occurred while trying to to load dictionary properties", e);
		}
	}

	protected void loadMessages() {
		Node root = NodeUtil.getNodeByPathOrNull(DictionaryConfiguration.REPOSITORY, "/");
		for (Locale locale : LocaleUtils.getLocalesOfAllSiteDefinitions()) {
			try {
				Properties properties = new Properties();
				for (Node message : NodeUtil.asList(NodeUtil.getNodes(root))) {
					String key = message.getName();
					String value = PropertyUtil.getString(message, LocaleUtils.getLocaleString(locale));
					if (key != null && value != null) {
						properties.put(key, value);
					}
				}
				messages.put(locale, properties);
			} catch (RepositoryException e) {
				LOG.warn("An error occurred while trying to to load dictionary properties with locale[{}]", locale, e);
			}
			LOG.debug("Loading dictionary properties with locale [{}]...", locale);
		}
	}

	public Map<Locale, Properties> getMessages() {
		return messages;
	}
}
