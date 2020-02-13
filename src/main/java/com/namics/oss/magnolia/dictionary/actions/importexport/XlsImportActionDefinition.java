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

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import info.magnolia.ui.api.action.CommandActionDefinition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines an action for saving an imported file.
 */
public class XlsImportActionDefinition extends CommandActionDefinition {

	public XlsImportActionDefinition() {
		setImplementationClass(XlsImportAction.class);
	}

	private Collection<String> properties = new ArrayList();

	private String nodeType = DictionaryConfiguration.NODE_TYPE;

	private boolean createNodes = false;

	public Collection<String> getProperties() {
		return properties;
	}

	public void setProperties(Collection<String> properties) {
		this.properties = properties;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public boolean isCreateNodes() {
		return createNodes;
	}

	public void setCreateNodes(boolean createNodes) {
		this.createNodes = createNodes;
	}
}
