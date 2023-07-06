package com.namics.oss.magnolia.dictionary.field;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.PropertysetItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * @author ploetscher, Namics AG
 * @since 21.03.2016
 */
public class MultiTextFieldFactory<D extends MultiTextFieldDefinition> extends AbstractFieldFactory<D, PropertysetItem> {

	private static final Logger LOG = LoggerFactory.getLogger(MultiTextFieldFactory.class);
	private FieldFactoryFactory fieldFactoryFactory;
	private ComponentProvider componentProvider;
	private final I18NAuthoringSupport i18nAuthoringSupport;

	@Inject
	public MultiTextFieldFactory(D definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, I18NAuthoringSupport i18nAuthoringSupport) {
		super(definition, relatedFieldItem);
		this.fieldFactoryFactory = fieldFactoryFactory;
		this.componentProvider = componentProvider;
		this.i18nAuthoringSupport = i18nAuthoringSupport;
	}

	@Override
	protected Field<PropertysetItem> createFieldComponent() {
		// FIXME change i18n setting : MGNLUI-1548
		definition.setI18nBasename(getMessages().getBasename());

		// we do not support multi text fields themselves to be required. Definition is overwritten here. Can't set it on the field after its creation cause otherwise the required asterisk is displayed.
		if (definition.isRequired()) {
			LOG.warn("Definition of the multi text field named [{}] is configured as required which is not supported.", definition.getName());
			definition.setRequired(false);
		}
		return new MultiTextField(definition, fieldFactoryFactory, componentProvider, item, i18nAuthoringSupport);
	}

	/**
	 * Create a new Instance of {@link Transformer}.
	 */
	@Override
	protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
		List<String> propertyNames = definition.getFieldNames();
		Transformer<?> transformer = this.componentProvider.newInstance(transformerClass, item, definition, PropertysetItem.class, propertyNames);
		transformer.setLocale(getLocale());
		return transformer;
	}
}
