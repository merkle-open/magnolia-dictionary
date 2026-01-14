package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.field.LabelJcrNodeProvider;

import jakarta.inject.Inject;

public class LabelNodeColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
    private final ConfiguredColumnDefinition<Item> columnDefinition;
    private final LabelJcrNodeProvider labelJcrNodeProvider;

    @Inject
    public LabelNodeColumnValueProvider(
            final ConfiguredColumnDefinition<Item> columnDefinition,
            final LabelJcrNodeProvider labelJcrNodeProvider
    ) {
        this.columnDefinition = columnDefinition;
        this.labelJcrNodeProvider = labelJcrNodeProvider;
    }

    @Override
    public String apply(final Item item) {
        if (item != null && item.isNode()) {
            final Node node = labelJcrNodeProvider.read((Node) item).orElse((Node) item);
            return PropertyUtil.getString(node, columnDefinition.getPropertyName());
        }
        return "";
    }
}
