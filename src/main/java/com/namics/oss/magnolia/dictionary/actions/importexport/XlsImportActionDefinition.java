package com.namics.oss.magnolia.dictionary.actions.importexport;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import info.magnolia.ui.api.action.CommandActionDefinition;


public class XlsImportActionDefinition extends CommandActionDefinition {

	public XlsImportActionDefinition() {
		setImplementationClass(XlsImportAction.class);
	}

	private String nodeType = DictionaryConfiguration.NODE_TYPE;

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
}
