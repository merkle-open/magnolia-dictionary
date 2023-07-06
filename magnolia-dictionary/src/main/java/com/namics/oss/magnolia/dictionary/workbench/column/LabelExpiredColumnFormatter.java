package com.namics.oss.magnolia.dictionary.workbench.column;

import com.vaadin.v7.ui.Table;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;

public class LabelExpiredColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> {

	@Inject
	public LabelExpiredColumnFormatter(ColumnDefinition definition) {
		super(definition);
	}

	@Override
	public Object generateCell(Table table, Object itemId, Object columnId) {
		Item jcrItem = getJcrItem(table, itemId);
		if (jcrItem != null && jcrItem.isNode()) {
			boolean isExpired = PropertyUtil.getBoolean((Node) jcrItem, "expired", false);
			if (isExpired) {
				return "╳";
			}
		}
		return null;
	}
}
