package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;

import javax.jcr.Item;

import com.merkle.oss.magnolia.dictionary.field.LabelJcrNodeProvider;
import com.merkle.oss.magnolia.powernode.PowerNodeService;

import jakarta.inject.Inject;

public class ExpiredColumnValueProvider extends LabelNodeColumnValueProvider {

    @Inject
    public ExpiredColumnValueProvider(
            final ConfiguredColumnDefinition<Item> columnDefinition,
            final LabelJcrNodeProvider labelJcrNodeProvider,
            final PowerNodeService powerNodeService
    ) {
        super(columnDefinition, labelJcrNodeProvider, powerNodeService);
    }

    @Override
    public String apply(final Item item) {
        if(Boolean.parseBoolean(super.apply(item))) {
            return "╳";
        }
        return "";
    }
}
