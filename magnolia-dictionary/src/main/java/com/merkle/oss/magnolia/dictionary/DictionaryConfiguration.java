package com.merkle.oss.magnolia.dictionary;

public final class DictionaryConfiguration {
	public static final String APP_NAME = "dictionary";
	public static final String EDIT_SUB_APP_NAME = "detail";
	public static final String REPOSITORY = "dictionary";
	public static final String LABEL_NODE_TYPE = "mgnl:label";
	public static final String SITE_SPECIFIC_LABEL_NODE_TYPE = "mgnl:siteSpecificLabel";

	public static final class Prop {
		public static final String VALUE = "value";
		public static final String NAME = "name";
		public static final String SITE = "site";
		public static final String EXPIRED = "expired";
	}

	public static final class ImportExport {
		public static final String FILENAME_TEMPLATE = "export-{0}-{1}.xlsx";
		public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd-HHmmss";
		public static final String SHEET_NAME = "export";
	}
}
