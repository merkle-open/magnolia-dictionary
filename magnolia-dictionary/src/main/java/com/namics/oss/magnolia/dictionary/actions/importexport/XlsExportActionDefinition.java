package com.namics.oss.magnolia.dictionary.actions.importexport;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

public class XlsExportActionDefinition extends ConfiguredActionDefinition {

	public XlsExportActionDefinition() {
		setImplementationClass(XlsExportAction.class);
	}

	private String nodeType = DictionaryConfiguration.NODE_TYPE;

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
}
