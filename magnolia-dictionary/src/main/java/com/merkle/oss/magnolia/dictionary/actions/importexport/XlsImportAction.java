package com.merkle.oss.magnolia.dictionary.actions.importexport;

import com.merkle.oss.magnolia.dictionary.services.XlsImportService;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;


public class XlsImportAction extends AbstractAction<XlsImportActionDefinition> {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private JcrNodeAdapter item;
	private EditorValidator validator;
	private EditorCallback callback;

	private XlsImportService importService;

	public XlsImportAction(XlsImportActionDefinition definition, JcrNodeAdapter item, EditorValidator validator, EditorCallback callback, XlsImportService importService) {
		super(definition);
		this.item = item;
		this.validator = validator;
		this.callback = callback;
		this.importService = importService;
	}

	@Override
	public void execute() throws ActionExecutionException {
		validator.showValidation(true);
		if (!validator.isValid()) {
			LOG.warn("Validation error(s) occurred. No Import performed.");
			return;
		}
		try {
			AbstractJcrNodeAdapter importXml = item.getChild("import");
			InputStream xlsStream = ((Binary) importXml.getItemProperty(JcrConstants.JCR_DATA).getValue()).getStream();
			importService.importXls(item.getWorkspace(), xlsStream);
		} catch (RepositoryException | IOException e) {
			throw new ActionExecutionException(e);
		}
		callback.onSuccess(getDefinition().getName());
	}
}
