package com.namics.oss.magnolia.dictionary;

import info.magnolia.jcr.util.NodeTypes;

import java.util.List;

public final class DictionaryConfiguration {
	public static final String REPOSITORY = "dictionary";
	public static final String NODE_TYPE = "mgnl:label";
	public static final String DICTIONARY_CONFIG_NODE_TYPE = "mgnl:dictionaryConfig";

	public static final class Prop {
		public static final String VALUE = "value";
		public static final String NAME = "name";
		public static final String EXPIRED = "expired";
	}

	public static final class ImportExport {
		public static final String JCR_NAME = "jcrName";
		public static final String REP_ROOT = "rep:root";
		public static final String FILENAME_TEMPLATE = "export-{0}-{1}.xlsx";
		public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd-HHmmss";
		public static final String SHEET_NAME = "export";
		public static final List<String> STATIC_EXPORT_PROPERTIES = List.of(
				JCR_NAME,
				Prop.NAME,
				Prop.VALUE
		);
		public static final List<String> DATE_EXPORT_PROPERTIES = List.of(
				NodeTypes.Created.NAME,
				NodeTypes.LastModified.NAME,
				NodeTypes.Activatable.LAST_ACTIVATED
		);
		public static final List<String> STATUS_EXPORT_PROPERTIES = List.of(
				NodeTypes.Activatable.ACTIVATION_STATUS
		);
		public static final List<String> STATIC_IMPORT_PROPERTIES = STATIC_EXPORT_PROPERTIES;
	}
}
