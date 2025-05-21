package com.merkle.oss.magnolia.dictionary.actions.delete;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.vaadin.ui.Notification;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.lang.invoke.MethodHandles;

public class DeleteExpiredLabelNodeAction extends AbstractAction<ConfiguredActionDefinition> {
	private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final JcrItemAdapter item;
	private final EventBus eventBus;

	@Inject
	public DeleteExpiredLabelNodeAction(ConfiguredActionDefinition definition, JcrItemAdapter item, @Named("admincentral") EventBus eventBus) {
		super(definition);
		this.item = item;
		this.eventBus = eventBus;
	}

	@Override
	public void execute() throws ActionExecutionException {
		if (!item.isNode()) {
			fail("The selected item does not seem to be a valid JCR node.");
			return;
		}

		Node labelNode = (Node) item.getJcrItem();

		if (!isExpired(labelNode)) {
			fail("The selected label is not expired.");
			return;
		}

		String labelName = PropertyUtil.getString(labelNode, DictionaryConfiguration.Prop.NAME, StringUtils.EMPTY);
		deleteLabel(labelNode);
		success(labelName);
	}

	private boolean isExpired(Node labelNode) {
		return PropertyUtil.getBoolean(labelNode, DictionaryConfiguration.Prop.EXPIRED, false);
	}

	private void deleteLabel(Node labelNode) throws ActionExecutionException {
		try {
			labelNode.remove();
			labelNode.getSession().save();
			eventBus.fireEvent(new ContentChangedEvent(JcrItemUtil.getItemId(item.getJcrItem())));
		} catch (RepositoryException e) {
			LOG.error("Could not delete node", e);
			throw new ActionExecutionException(e);
		}
	}

	private void success(String info) {
		AlertBuilder.alert("delete expired node")
				.withLevel(Notification.Type.ASSISTIVE_NOTIFICATION)
				.withTitle("Selected label was deleted")
				.withBody("The following label was deleted: " + info)
				.withOkButtonCaption("Ok")
				.buildAndOpen();
	}

	private void fail(String reason) {
		AlertBuilder.alert("delete expired node")
				.withLevel(Notification.Type.ERROR_MESSAGE)
				.withTitle("Can't delete selected label")
				.withBody(reason)
				.withOkButtonCaption("Ok")
				.buildAndOpen();
	}

}
