package com.namics.oss.magnolia.dictionary.action;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import com.namics.oss.magnolia.dictionary.util.predicates.LabelExpiredPredicate;
import com.namics.oss.magnolia.dictionary.util.predicates.SystemNodeFilteringPredicate;
import com.vaadin.ui.Notification;
import info.magnolia.event.EventBus;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.lang.invoke.MethodHandles;

public class DeleteAllExpiredLabelNodeAction extends AbstractAction<ConfiguredActionDefinition> {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final EventBus eventBus;

	@Inject
	public DeleteAllExpiredLabelNodeAction(ConfiguredActionDefinition definition, @Named("admincentral") EventBus eventBus) {
		super(definition);
		this.eventBus = eventBus;
	}

	@Override
	public void execute() throws ActionExecutionException {
		try {
			Node dictionaryRoot = NodeUtil.getWorkspaceRootNode(DictionaryConfiguration.REPOSITORY);
			var expiredNodesPredicate = Predicates.and(new LabelExpiredPredicate(), new SystemNodeFilteringPredicate());
			Iterable<Node> expiredNodes = NodeUtil.getNodes(dictionaryRoot, expiredNodesPredicate);
			for (Node expiredNode : expiredNodes) {
				JcrItemId itemId = JcrItemUtil.getItemId(expiredNode);
				expiredNode.remove();
				eventBus.fireEvent(new ContentChangedEvent(itemId));
			}
			dictionaryRoot.getSession().save();
		} catch (RepositoryException e) {
			LOG.error("Could not delete nodes.", e);
			throw new ActionExecutionException(e);
		}

		AlertBuilder.alert("delete expired nodes")
				.withLevel(Notification.Type.ASSISTIVE_NOTIFICATION)
				.withTitle("Deleted expired labels")
				.withBody("All expired labels were deleted")
				.withOkButtonCaption("Ok")
				.buildAndOpen();
	}
}
