package com.namics.oss.magnolia.dictionary.action;

import com.namics.mgnl.commons.utils.PropertyUtil;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

public class DeleteExpiredLabelNodeAction extends AbstractAction<ConfiguredActionDefinition> {
	private Logger LOG = LoggerFactory.getLogger(DeleteExpiredLabelNodeAction.class);

	private UiContext uiContext;
	List<JcrItemAdapter> items;
	EventBus eventBus;
	private boolean stillInUse = false;
	private final Set<JcrItemId> changedItemIds = new HashSet<>();

	public DeleteExpiredLabelNodeAction(ConfiguredActionDefinition definition,
	                                    JcrItemAdapter item,
	                                    @Named("admincentral") EventBus eventBus,
	                                    UiContext uiContext) {
		this(definition, Collections.singletonList(item), eventBus, uiContext);
	}

	public DeleteExpiredLabelNodeAction(ConfiguredActionDefinition definition,
	                                    List<JcrItemAdapter> items,
	                                    @Named("admincentral") EventBus eventBus,
	                                    UiContext uiContext) {
		super(definition);
		this.uiContext = uiContext;
		this.items = items;
		this.eventBus = eventBus;
	}

	@Override
	public void execute() throws ActionExecutionException {
		try {
			Optional<Long> lastLoadedTime = DictionaryUtils.getLastLoadedTime();
			lastLoadedTime.ifPresent(lastLoaded -> items.forEach(item -> {
				try {
					Item jcrItem = item.getJcrItem();
					if (jcrItem.isNode()) {
						Node itemNode = (Node) jcrItem;
						Long lastModified = PropertyUtil.getLong(itemNode, NodeTypes.LastModified.LAST_MODIFIED);
						if (lastModified != null) {
							if (lastModified < lastLoaded) {
								itemNode.remove();
								itemNode.getSession().save();
								changedItemIds.add(JcrItemUtil.getItemId(jcrItem));
							} else {
								stillInUse = true;
							}
						} else {
							LOG.error("last modified time not set on node {}. this should never occur.", itemNode);
						}
					}
				} catch (RepositoryException e) {
					LOG.error("Could not read property.", e);
				}
			}));
		} catch (RepositoryException e) {
			LOG.error("Could not read last loaded time.", e);
		} finally {
			if (stillInUse) {
				uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, "Selected label is still used.");
			}

			if (CollectionUtils.isNotEmpty(changedItemIds)) {
				eventBus.fireEvent(new ContentChangedEvent(changedItemIds));
				uiContext.openNotification(MessageStyleTypeEnum.INFO, false, "Selected expired label deleted.");
			}
		}
	}
}
