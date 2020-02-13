package com.namics.oss.magnolia.dictionary.actions.importexport;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

import java.util.ArrayList;
import java.util.Collection;

public class XlsExportActionDefinition extends ConfiguredActionDefinition {
	public XlsExportActionDefinition() {
		setImplementationClass(XlsExportAction.class);
	}

	private Collection<String> properties = new ArrayList();

	private String nodeType = DictionaryConfiguration.NODE_TYPE;

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
}
