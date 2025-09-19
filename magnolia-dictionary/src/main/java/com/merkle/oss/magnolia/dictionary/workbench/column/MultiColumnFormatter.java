package com.merkle.oss.magnolia.dictionary.workbench.column;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;
import com.vaadin.v7.ui.Table;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import java.util.Locale;

/**
 * @author mrauch, Namics AG
 * @since 15.11.2016
 */
public class MultiColumnFormatter extends AbstractColumnFormatter<PropertyColumnDefinition> {

	private static final String EMPTY_PLACEHOLDER = "-";
	private static final String SEPARATOR = "|";

	public MultiColumnFormatter(PropertyColumnDefinition definition) {
		super(definition);
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		Item jcrItem = this.getJcrItem(source, itemId);
		if (jcrItem != null && jcrItem.isNode()) {
			Node node = (Node) jcrItem;

			String displayedText = StringUtils.EMPTY;
			for (Locale locale : LocaleUtils.getLocalesOfAllSiteDefinitions()) {
				if (StringUtils.isNotEmpty(displayedText)) {
					displayedText += SEPARATOR;
				}
				displayedText += PropertyUtil.getString(node, LocaleUtils.getLocaleString(locale), EMPTY_PLACEHOLDER);
			}

			return displayedText;
		}

		return null;
	}
}
