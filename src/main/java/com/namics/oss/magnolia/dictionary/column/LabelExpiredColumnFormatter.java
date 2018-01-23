package com.namics.oss.magnolia.dictionary.column;

import com.namics.mgnl.commons.utils.PropertyUtil;
import com.namics.oss.magnolia.dictionary.util.DictionaryUtils;
import com.vaadin.v7.ui.Table;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

public class LabelExpiredColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> {
    private static final Logger LOG = LoggerFactory.getLogger(LabelExpiredColumnFormatter.class);

    @Inject
    public LabelExpiredColumnFormatter(ColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table table, Object itemId, Object columnId) {
        try {
            Optional<Long> lastLoadedTime = DictionaryUtils.getLastLoadedTime();
            if (lastLoadedTime.isPresent()) {
                Item jcrItem = getJcrItem(table, itemId);
                if (jcrItem != null && jcrItem.isNode()) {
                    Long lastModified = PropertyUtil.getLong((Node) jcrItem, NodeTypes.LastModified.LAST_MODIFIED);
                    if (lastLoadedTime != null) {
                        if (lastModified < lastLoadedTime.get()) {
                            return "<span style=\"background-color:darkred;\">1</span>";
                        }
                    } else {
                        LOG.error("last modified time not set on node {}. this should never occur.", jcrItem);
                    }
                }
            }
        } catch (RepositoryException e) {

        }
        return null;
    }
}
