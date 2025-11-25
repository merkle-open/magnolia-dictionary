package com.merkle.oss.magnolia.dictionary.actions.delete;

import static com.vaadin.shared.Position.*;
import static com.vaadin.ui.Notification.DELAY_FOREVER;
import static com.vaadin.ui.Notification.Type.*;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.contentapp.action.CommandAction;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.util.Notification;

import java.lang.invoke.MethodHandles;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.NodeUtil;
import com.merkle.oss.magnolia.dictionary.util.predicates.LabelExpiredPredicate;
import com.merkle.oss.magnolia.dictionary.util.predicates.SystemNodeFilteringPredicate;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class DeleteAllExpiredLabels extends CommandAction<Node, DeleteAllExpiredLabels.Definition> {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Provider<Notification> notificationProvider;

    @Inject
	public DeleteAllExpiredLabels(
            final Definition definition,
            final CommandsManager commandsManager,
            final ValueContext<Node> valueContext,
            final Context context,
            final AsyncActionExecutor asyncActionExecutor,
            final DatasourceObservation.Manual datasourceObservation,
            final Provider<Notification> notificationProvider
    ) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor, datasourceObservation);
        this.notificationProvider = notificationProvider;
    }

	@Override
	public void execute() {
		try {
            final Node dictionaryRoot = NodeUtil.getWorkspaceRootNode(DictionaryConfiguration.REPOSITORY);
            final Predicate expiredNodesPredicate = Predicates.and(new LabelExpiredPredicate(), new SystemNodeFilteringPredicate());
			final Iterable<Node> expiredNodes = NodeUtil.getNodes(dictionaryRoot, expiredNodesPredicate);
			for (Node expiredNode : expiredNodes) {
				expiredNode.remove();
			}
			dictionaryRoot.getSession().save();
            notificationProvider.get()
                    .withCaption("All expired labels were deleted")
                    .withStyle(ASSISTIVE_NOTIFICATION)
                    .withPosition(MIDDLE_CENTER)
                    .show();
		} catch (RepositoryException e) {
			LOG.error("Could not delete nodes.", e);
            notificationProvider.get()
                    .withCaption(e.getMessage())
                    .withStyle(WARNING_MESSAGE)
                    .withPosition(MIDDLE_CENTER)
                    .withDelayMsec(DELAY_FOREVER)
                    .show();
		}
	}

    public static class Definition extends CommandActionDefinition {
        public Definition() {
            setImplementationClass(DeleteAllExpiredLabels.class);
        }
    }
}
