package com.merkle.oss.magnolia.dictionary.field;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.PropertysetItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.AbstractCustomMultiField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

/**
 * @author ploetscher, Namics AG
 * @since 21.03.2016
 */
public class MultiTextField extends AbstractCustomMultiField<MultiTextFieldDefinition, PropertysetItem> {

	public MultiTextField(MultiTextFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, Item relatedFieldItem, I18NAuthoringSupport i18nAuthoringSupport) {
		super(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, i18nAuthoringSupport);
	}

	@Override
	protected Component initContent() {
		// Init root layout
		addStyleName("linkfield");
		if (definition.getLayout() == Layout.horizontal) {
			root = new HorizontalLayout();
		} else {
			root = new VerticalLayout();
		}

		// Initialize Existing field
		initFields();
		return root;
	}

	@Override
	protected void initFields(PropertysetItem fieldValues) {
		root.removeAllComponents();

		for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
			// Only propagate read only if the parent definition is read only
			if (definition.isReadOnly()) {
				fieldDefinition.setReadOnly(true);
			}
			Field<?> multiTextField = createLocalField(fieldDefinition, fieldValues.getItemProperty(fieldDefinition.getName()), false);
			if (fieldValues.getItemProperty(fieldDefinition.getName()) == null) {
				fieldValues.addItemProperty(fieldDefinition.getName(), multiTextField.getPropertyDataSource());
			}
			multiTextField.setWidth(100, Unit.PERCENTAGE);

			root.addComponent(multiTextField);
		}
	}

	@Override
	public Class<? extends PropertysetItem> getType() {
		return PropertysetItem.class;
	}
}