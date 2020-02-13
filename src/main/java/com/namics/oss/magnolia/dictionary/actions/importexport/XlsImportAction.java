/**
 * This file Copyright (c) 2013-2016 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 * <p>
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 * <p>
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package com.namics.oss.magnolia.dictionary.actions.importexport;

import com.namics.oss.magnolia.dictionary.services.XlsExportService;
import com.vaadin.v7.data.Item;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Call Import Command in order to perform the import action.
 */
public class XlsImportAction extends AbstractAction<XlsImportActionDefinition> {

	private static final Logger LOG = LoggerFactory.getLogger(XlsImportAction.class);

	private final Item item;
	private EditorValidator validator;
	private EditorCallback callback;

	XlsExportService exportService;

	public XlsImportAction(XlsImportActionDefinition definition, final Item item, final EditorValidator validator, final EditorCallback callback, XlsExportService exportService) {
		super(definition);
		this.item = item;
		this.validator = validator;
		this.callback = callback;
		this.exportService = exportService;
	}

	@Override
	public void execute() throws ActionExecutionException {
		// First Validate
		validator.showValidation(true);
		if (validator.isValid()) {
			final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;
			JcrNodeAdapter importXls = (JcrNodeAdapter) itemChanged.getChild("import");
			if (importXls != null) {
				try {
					JcrNodeAdapter importXml = (JcrNodeAdapter) itemChanged.getChild("import");
					InputStream xlsStream = ((BinaryImpl) importXml.getItemProperty(JcrConstants.JCR_DATA).getValue()).getStream();
					exportService.importXls(itemChanged.getWorkspace(), itemChanged.getJcrItem().getPath(), getDefinition().isCreateNodes(), xlsStream);
				} catch (Exception e) {
					throw new ActionExecutionException(e);
				}
			}
			callback.onSuccess(getDefinition().getName());
		} else {
			LOG.info("Validation error(s) occurred. No Import performed.");
		}
	}
}
