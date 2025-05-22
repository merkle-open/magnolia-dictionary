package com.merkle.oss.magnolia.dictionary.field;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertysetItem;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import java.util.Iterator;
import java.util.List;

/**
 * @author ploetscher, Namics AG
 * @since 21.03.2016
 */
public class MultiTextFieldTransformer extends BasicTransformer<PropertysetItem> {

	protected List<String> fieldNames;

	public MultiTextFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldNames, I18NAuthoringSupport i18NAuthoringSupport) {
		super(relatedFormItem, definition, type, i18NAuthoringSupport);
		this.fieldNames = fieldNames;
	}

	@Override
	public void writeToItem(PropertysetItem newValues) {
		// Get iterator.
		Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();

		while (propertyNames.hasNext()) {
			String propertyName = (String) propertyNames.next();
			String compositePropertyName = getCompositePropertyName(propertyName);
			Property<?> property = relatedFormItem.getItemProperty(compositePropertyName);
			if (property == null && newValues.getItemProperty(propertyName) != null) {
				relatedFormItem.addItemProperty(compositePropertyName, newValues.getItemProperty(propertyName));
			}
		}
	}

	@Override
	public PropertysetItem readFromItem() {
		PropertysetItem newValues = new PropertysetItem();
		for (String propertyName : fieldNames) {
			String compositePropertyName = getCompositePropertyName(propertyName);
			if (relatedFormItem.getItemProperty(compositePropertyName) != null) {
				newValues.addItemProperty(propertyName, relatedFormItem.getItemProperty(compositePropertyName));
			}
		}
		return newValues;
	}

	protected String getCompositePropertyName(String propertyName) {
		return deriveLocaleAwareName(propertyName);
	}
}
