package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

public class ExpiredColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
    @Override
    public String apply(final Item item) {
        if (item != null && item.isNode()) {
            final Node node = (Node) item;
            final boolean isExpired = PropertyUtil.getBoolean(node, DictionaryConfiguration.Prop.EXPIRED, false);
            if (isExpired) {
                return "╳";
            }
        }
        return "";
    }
}
