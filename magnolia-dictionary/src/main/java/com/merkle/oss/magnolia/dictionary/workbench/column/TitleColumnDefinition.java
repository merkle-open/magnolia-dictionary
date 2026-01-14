package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.contentapp.column.jcr.JcrTitleComponentValueProvider;
import info.magnolia.ui.contentapp.configuration.column.ComponentColumnDefinition;
import info.magnolia.ui.filter.FilterContext;

import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

import jakarta.inject.Inject;

public class TitleColumnDefinition extends ComponentColumnDefinition<Item> {

    public TitleColumnDefinition() {
        setValueProvider(TitleValueProvider.class);
        setEditable(false);
        setNodeTypeToComponents(Map.of(
                DictionaryConfiguration.LABEL_NODE_TYPE, getNodeTypeComponent(MagnoliaIcons.EDIT),
                DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE, getNodeTypeComponent(MagnoliaIcons.APP)
        ));
    }

    private CellComponentConfiguration getNodeTypeComponent(final MagnoliaIcons icon) {
        final ComponentColumnDefinition.CellComponentConfiguration cellComponentConfiguration = new ComponentColumnDefinition.CellComponentConfiguration();
        cellComponentConfiguration.setIcon(icon.getCssClass());
        cellComponentConfiguration.setShowPath(false);
        return cellComponentConfiguration;
    }

    public static class TitleValueProvider extends JcrTitleComponentValueProvider {
        @Inject
        public TitleValueProvider(final TitleColumnDefinition definition, final FilterContext filterContext) {
            super(definition, filterContext);
        }

        @Override
        protected String getName(final Item item) {
            try {
                final Node node = (Node) item;
                if (node.isNodeType(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE)) {
                    return PropertyUtil.getString(node.getParent(), DictionaryConfiguration.Prop.NAME) + " - " + PropertyUtil.getString(node, DictionaryConfiguration.Prop.SITE);
                }
                return PropertyUtil.getString(node, DictionaryConfiguration.Prop.NAME);
            } catch (Exception e) {
                return super.getName(item);
            }
        }
    }
}
