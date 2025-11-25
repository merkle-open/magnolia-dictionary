package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.jcr.util.PropertyUtil;

import java.util.stream.Collectors;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;

import jakarta.inject.Inject;

public class MultiColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
	private static final String EMPTY_PLACEHOLDER = "-";
	private static final String SEPARATOR = "|";
    private final LocaleUtils localeUtils;

    @Inject
    public MultiColumnValueProvider(final LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Override
    public String apply(final Item item) {
        if (item != null && item.isNode()) {
            final Node node = (Node) item;
            return localeUtils.streamLocalesOfAllSites().map(locale ->
                    PropertyUtil.getString(node, localeUtils.getLocaleString(locale), EMPTY_PLACEHOLDER)
            ).collect(Collectors.joining(SEPARATOR));
        }
        return "";
    }
}
