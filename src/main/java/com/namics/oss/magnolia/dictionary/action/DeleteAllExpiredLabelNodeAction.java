package com.namics.oss.magnolia.dictionary.action;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.AbstractRepositoryAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

public class DeleteAllExpiredLabelNodeAction extends AbstractRepositoryAction<ConfiguredActionDefinition> {
	private static final Logger LOG = LoggerFactory.getLogger(DeleteAllExpiredLabelNodeAction.class);

	private UiContext uiContext;

	@Inject
	protected DeleteAllExpiredLabelNodeAction(ConfiguredActionDefinition definition,
	                                          JcrItemAdapter item,
	                                          @Named("admincentral") EventBus eventBus,
	                                          UiContext uiContext) {
		super(definition, item, eventBus);
		this.uiContext = uiContext;
	}

	@Override
	protected void onExecute(JcrItemAdapter jcrItemAdapter) throws RepositoryException {
		Optional<Long> lastLoadedTime = DictionaryUtils.getLastLoadedTime();
		if (lastLoadedTime.isPresent()) {
			Node rootNode = NodeUtil.getWorkspaceRootNode(DictionaryConfiguration.REPOSITORY);
			Iterable<Node> allChildNodes = info.magnolia.jcr.util.NodeUtil.collectAllChildren(rootNode);
			for (Node childNode : allChildNodes) {
				Long lastModified = PropertyUtil.getLong(childNode, NodeTypes.LastModified.LAST_MODIFIED);
				if (lastModified != null) {
					if (lastModified < lastLoadedTime.get()) {
						childNode.remove();
					}
				} else {
					LOG.error("last modified time not set on node {}. this should never occur.", childNode);
				}
			}
		}
		uiContext.openNotification(MessageStyleTypeEnum.INFO, false, "Expired Labels deleted.");
	}
}
