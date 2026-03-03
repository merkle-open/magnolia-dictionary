package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.module.site.Site;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.editor.CurrentItemProviderDefinition;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.TextFieldDefinition;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.powernode.PowerNode;

public class TranslationsTextFieldDefinition extends CompositeFieldDefinition<Node> {
    private final SiteProvider siteProvider;
    private final LocaleUtil localeUtils;
    private final LocationBasedNodeProvider locationBasedNodeProvider;

    public TranslationsTextFieldDefinition() {
        setItemProvider(new CurrentItemProviderDefinition<>());
        this.siteProvider = Components.getComponent(SiteProvider.class);
        this.localeUtils = Components.getComponent(LocaleUtil.class);
        this.locationBasedNodeProvider = Components.getComponent(LocationBasedNodeProvider.class);
    }

    @Override
    public List<EditorPropertyDefinition> getProperties() {
        final Site site = locationBasedNodeProvider.getNode()
                .map(PowerNode::getName)
                .flatMap(siteProvider::getSite)
                .orElseGet(siteProvider::getGenericSite);
        return site.getI18n().getLocales().stream()
                .sorted(Comparator.comparing(Locale::getLanguage))
                .map(this::getField)
                .collect(Collectors.toList());
    }

    private TextFieldDefinition getField(final Locale locale) {
        final TextFieldDefinition textField = new TextFieldDefinition();
        textField.setName(localeUtils.toLocaleString(locale));
        textField.setLabel(locale.getDisplayName());
        textField.setPlaceholder(locale.getDisplayName());
        return textField;
    }
}
