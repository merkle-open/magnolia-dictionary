package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.editor.CurrentItemProviderDefinition;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.TextFieldDefinition;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;

public class MultiTextFieldDefinition<T> extends CompositeFieldDefinition<T> {
    private final LocaleUtils localeUtils;

    public MultiTextFieldDefinition() {
        setItemProvider(new CurrentItemProviderDefinition<>());
        this.localeUtils = Components.getComponent(LocaleUtils.class);
    }

    @Override
    public List<EditorPropertyDefinition> getProperties() {
        return localeUtils.streamLocalesOfAllSites()
                .map(this::getField)
                .collect(Collectors.toList());
    }

    private TextFieldDefinition getField(final Locale locale) {
        final TextFieldDefinition textField = new TextFieldDefinition();
        textField.setName(localeUtils.getLocaleString(locale));
        textField.setLabel(locale.getDisplayName());
        textField.setPlaceholder(locale.getDisplayName());
        return textField;
    }
}
