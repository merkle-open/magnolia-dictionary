package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;

import java.util.Optional;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.field.LabelJcrNodeProvider;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Inject;

public class LabelNodeColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
    private final ConfiguredColumnDefinition<Item> columnDefinition;
    private final LabelJcrNodeProvider labelJcrNodeProvider;
    private final PowerNodeService powerNodeService;

    @Inject
    public LabelNodeColumnValueProvider(
            final ConfiguredColumnDefinition<Item> columnDefinition,
            final LabelJcrNodeProvider labelJcrNodeProvider,
            final PowerNodeService powerNodeService
    ) {
        this.columnDefinition = columnDefinition;
        this.labelJcrNodeProvider = labelJcrNodeProvider;
        this.powerNodeService = powerNodeService;
    }

    @Override
    public String apply(final Item item) {
        return Optional
                .ofNullable(item)
                .filter(Item::isNode)
                .map(i -> (Node)i)
                .map(node -> labelJcrNodeProvider.read(node).orElse(node))
                .map(powerNodeService::convertToPowerNode)
                .flatMap(node -> node.getProperty(columnDefinition.getPropertyName(), ValueConverter::getString))
                .orElse("");
    }
}
