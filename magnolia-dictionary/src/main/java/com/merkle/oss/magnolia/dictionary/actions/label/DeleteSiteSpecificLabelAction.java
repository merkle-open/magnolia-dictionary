package com.merkle.oss.magnolia.dictionary.actions.label;

import static com.vaadin.shared.Position.MIDDLE_CENTER;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.util.Notification;

import javax.jcr.Node;

import com.google.inject.Inject;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;

import jakarta.inject.Provider;

public class DeleteSiteSpecificLabelAction extends JcrCommandAction<Node, DeleteSiteSpecificLabelAction.Definition> {
    private final Provider<Notification> notificationProvider;
    private final PowerNodeService powerNodeService;

    @Inject
    public DeleteSiteSpecificLabelAction(
            final DeleteSiteSpecificLabelAction.Definition definition,
            final CommandsManager commandsManager,
            final ValueContext<Node> valueContext,
            final Context context,
            final AsyncActionExecutor asyncActionExecutor,
            final JcrDatasource jcrDatasource,
            final DatasourceObservation.Manual datasourceObservation,
            final Provider<Notification> notificationProvider,
            final PowerNodeService powerNodeService
    ) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor, jcrDatasource, datasourceObservation);
        this.notificationProvider = notificationProvider;
        this.powerNodeService = powerNodeService;
    }

    @Override
    public void execute() {
        try {
            final PowerNode siteNode = powerNodeService.convertToPowerNode(getValueContext().getSingleOrThrow());
            if (!siteNode.isNodeType(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE)) {
                throw new IllegalStateException("The selected label is not a site node.");
            }
            siteNode.remove();
            siteNode.getSession().save();
        } catch (Exception e) {
            notificationProvider.get()
                    .withCaption(e.getMessage())
                    .withStyle(WARNING_MESSAGE)
                    .withPosition(MIDDLE_CENTER)
                    .show();
        }
    }

    public static class Definition extends JcrCommandActionDefinition {
        public Definition() {
            setImplementationClass(DeleteSiteSpecificLabelAction.class);
        }
    }
}
