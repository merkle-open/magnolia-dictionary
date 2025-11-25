package com.merkle.oss.magnolia.dictionary.actions.importexport;

import static com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport.FILENAME_TEMPLATE;
import static com.vaadin.shared.Position.*;
import static com.vaadin.ui.Notification.DELAY_FOREVER;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.framework.util.TempFileStreamResource;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.util.DownloadHelper;
import info.magnolia.util.Notification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.services.XlsExportService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class XlsExportAction extends JcrCommandAction<Node, XlsExportAction.Definition> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DictionaryConfiguration.ImportExport.DATE_FORMAT_PATTERN);
    private final DownloadHelper downloadHelper;
    private final Provider<Notification> notificationProvider;
    private final XlsExportService exportService;
    private TempFileStreamResource tempFileStreamResource;

    @Inject
	public XlsExportAction(
            final Definition definition,
            final CommandsManager commandsManager,
            final ValueContext<Node> valueContext,
            final Context context,
            final AsyncActionExecutor asyncActionExecutor,
            final JcrDatasource jcrDatasource,
            final DatasourceObservation.Manual datasourceObservation,
            final DownloadHelper downloadHelper,
            final Provider<Notification> notificationProvider,
            final XlsExportService exportService
    ) {
		super(definition, commandsManager, valueContext, context, asyncActionExecutor, jcrDatasource, datasourceObservation);
        this.downloadHelper = downloadHelper;
        this.notificationProvider = notificationProvider;
        this.exportService = exportService;
	}

	@Override
	public void execute() {
        final Collection<Node> nodes = resolveTargetItems();
        try (ByteArrayOutputStream outputStream = exportService.exportXls(nodes)) {
            tempFileStreamResource = new TempFileStreamResource();
            tempFileStreamResource.setStreamSource(new Resource(new ByteArrayInputStream(outputStream.toByteArray())));
            final String filename = getFilename();
            tempFileStreamResource.setTempFileName(filename);
            tempFileStreamResource.setFilename(filename);
            tempFileStreamResource.setMIMEType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            downloadHelper.download(tempFileStreamResource);
        } catch (Exception e) {
            LOG.error("Failed to export", e);
            notificationProvider.get()
                    .withCaption(e.getMessage())
                    .withStyle(WARNING_MESSAGE)
                    .withPosition(MIDDLE_CENTER)
                    .withDelayMsec(DELAY_FOREVER)
                    .show();
        }
	}

    private String getFilename() {
        final String formattedDate = FORMATTER.withZone(ZoneId.systemDefault()).format(Instant.now());
        return MessageFormat.format(FILENAME_TEMPLATE, DictionaryConfiguration.REPOSITORY, formattedDate);
    }

    @Override
    protected Map<String, Object> buildParams(final Node jcrItem) {
        try {
            final Map<String, Object> params = super.buildParams(jcrItem);
            params.put("format", "xlsx");
            params.put("outputStream", tempFileStreamResource.getTempFileOutputStream());
            return params;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to bind command to temp file output stream: ", e);
        }
    }

    private static class Resource extends TempFileStreamResource.TempFileStreamSource {
        private final InputStream inputStream;
        public Resource(final InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public InputStream getStream() {
            return inputStream;
        }
    }

    public static class Definition extends JcrCommandActionDefinition {
        public Definition() {
            setImplementationClass(XlsExportAction.class);
        }
    }
}
