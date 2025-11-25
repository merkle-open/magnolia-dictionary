package com.merkle.oss.magnolia.dictionary.actions.importexport;

import static com.vaadin.shared.Position.MIDDLE_CENTER;
import static com.vaadin.ui.Notification.DELAY_FOREVER;
import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;
import info.magnolia.ui.contentapp.action.JcrCommandParamsResolver;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.util.Notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

import javax.jcr.Node;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.services.XlsImportService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;


public class XlsImportAction  extends JcrCommandAction<Node, XlsImportAction.Definition> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CloseHandler closeHandler;
    private final FormView<Node> form;
    private final Provider<Notification> notificationProvider;
    private final XlsImportService importService;

    @Inject
	public XlsImportAction(
            final Definition definition,
            final CommandsManager commandsManager,
            final ValueContext<Node> valueContext,
            final Context context,
            final AsyncActionExecutor asyncActionExecutor,
            final JcrDatasource jcrDatasource,
            final DatasourceObservation.Manual datasourceObservation,
            final CloseHandler closeHandler,
            final FormView<Node> form,
            final Provider<Notification> notificationProvider,
            final XlsImportService importService
    ) {
		super(definition, commandsManager, valueContext, context, asyncActionExecutor, jcrDatasource, datasourceObservation);
        this.closeHandler = closeHandler;
        this.form = form;
        this.notificationProvider = notificationProvider;
        this.importService = importService;
	}

	@Override
	public void execute() {
        getUploadedFile().ifPresent(file -> {
            try {
                final InputStream xlsStream = new FileInputStream(file);
                importService.importXls(DictionaryConfiguration.REPOSITORY, xlsStream);
                closeHandler.close();
            } catch (Exception e) {
                LOG.error("Failed to import", e);
                notificationProvider.get()
                        .withCaption(e.getMessage())
                        .withStyle(ERROR_MESSAGE)
                        .withPosition(MIDDLE_CENTER)
                        .withDelayMsec(DELAY_FOREVER)
                        .withHtmlContentAllowed()
                        .show();
            }
        });
	}

    @Override
    protected Map<String, Object> buildParams(Node item) {
        Map<String, Object> params = super.buildParams(item);
        getUploadedFile().ifPresent(file -> {
            try {
                params.put("stream", FileUtils.openInputStream(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            params.put("fileName", file.getName());
        });
        return params;
    }

    private Optional<File> getUploadedFile() {
        return form.getPropertyValue("contentStream");
    }

    public static class Definition extends JcrCommandActionDefinition {
        public Definition() {
            setImplementationClass(XlsImportAction.class);
        }
    }
}
