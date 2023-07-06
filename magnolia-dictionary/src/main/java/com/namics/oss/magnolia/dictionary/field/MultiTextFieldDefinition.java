package com.namics.oss.magnolia.dictionary.field;

import com.google.common.collect.Lists;
import com.namics.oss.magnolia.dictionary.util.LocaleUtils;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author ploetscher, Namics AG
 * @since 16.03.2016
 */
public class MultiTextFieldDefinition extends TextFieldDefinition {

	protected Layout layout;

	public MultiTextFieldDefinition() {
		layout = Layout.vertical;
		setTransformerClass(MultiTextFieldTransformer.class);
	}

	public List<ConfiguredFieldDefinition> getFields() {
		List<ConfiguredFieldDefinition> fields = new ArrayList<>();

		List<Locale> locales = LocaleUtils.getLocalesOfAllSiteDefinitions();
		for (Locale locale : locales) {
			TextFieldDefinition textField = new TextFieldDefinition();
			textField.setMaxLength(this.getMaxLength());
			textField.setRows(this.getRows());
			textField.setReadOnly(this.isReadOnly());
			textField.setRequired(this.isRequired());
			textField.setDefaultValue(this.getDefaultValue());
			textField.setStyleName(this.getStyleName());
			textField.setRequiredErrorMessage(this.getRequiredErrorMessage());
			textField.setType(this.getType());
			textField.setValidators(this.getValidators());
			textField.setName(LocaleUtils.getLocaleString(locale));
			textField.setLabel(locale.getDisplayName());
			textField.setPlaceholder(locale.getDisplayName());
			fields.add(textField);
		}

		return fields;
	}

	/**
	 * @return an unmodifiable list of the field names.
	 */
	public List<String> getFieldNames() {
		// List#transform gives a view on the original list; we don't want client usages to mess with it
		return Collections.unmodifiableList(Lists.transform(getFields(), ConfiguredFieldDefinition::getName));
	}

	/**
	 * @return desired select part layout.
	 */
	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}
}
