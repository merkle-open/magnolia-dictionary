package com.merkle.oss.magnolia.dictionary.actions.delete;

import static com.vaadin.shared.Position.MIDDLE_CENTER;
import static com.vaadin.ui.Notification.Type.*;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.util.Notification;

import java.lang.invoke.MethodHandles;

import javax.jcr.Node;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Provider;

public class DeleteExpiredLabel extends JcrCommandAction<Node, DeleteExpiredLabel.Definition> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Provider<Notification> notificationProvider;
    private final PowerNodeService powerNodeService;

    @Inject
	public DeleteExpiredLabel(
            final Definition definition,
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
            final PowerNode labelNode = powerNodeService.convertToPowerNode(getValueContext().getSingleOrThrow());
            if (!isExpired(labelNode)) {
                throw new IllegalStateException("The selected label is not expired.");
            } else {
                final String labelName = labelNode.getProperty(DictionaryConfiguration.Prop.NAME, ValueConverter::getString).orElse(StringUtils.EMPTY);
                labelNode.remove();
                labelNode.getSession().save();
                notificationProvider.get()
                        .withCaption("The following label was deleted: " + labelName)
                        .withStyle(ASSISTIVE_NOTIFICATION)
                        .withPosition(MIDDLE_CENTER)
                        .show();
            }
        } catch (Exception e) {
            LOG.error("Failed to export", e);
            notificationProvider.get()
                    .withCaption(e.getMessage())
                    .withStyle(WARNING_MESSAGE)
                    .withPosition(MIDDLE_CENTER)
                    .show();
        }
	}

	private boolean isExpired(PowerNode labelNode) {
		return labelNode.getProperty(DictionaryConfiguration.Prop.EXPIRED, ValueConverter::getBoolean).orElse(false);
	}

    public static class Definition extends JcrCommandActionDefinition {
        public Definition() {
            setImplementationClass(DeleteExpiredLabel.class);
        }
    }
}
