package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.ui.contentapp.column.jcr.JcrTitleComponentValueProvider;
import info.magnolia.ui.contentapp.configuration.column.ComponentColumnDefinition;
import info.magnolia.ui.filter.FilterContext;

import java.util.Map;
import java.util.Optional;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

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
        private final PowerNodeService powerNodeService;

        @Inject
        public TitleValueProvider(
                final TitleColumnDefinition definition,
                final FilterContext filterContext,
                final PowerNodeService powerNodeService
        ) {
            super(definition, filterContext);
            this.powerNodeService = powerNodeService;
        }

        @Override
        protected String getName(final Item item) {
            return Optional
                    .ofNullable(item)
                    .filter(Item::isNode)
                    .map(i -> (Node)i)
                    .map(powerNodeService::convertToPowerNode)
                    .flatMap(this::getName)
                    .orElseGet(() -> super.getName(item));
        }

        private Optional<String> getName(final PowerNode node) {
            if(node.isNodeType(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE)) {
                return node.getProperty(DictionaryConfiguration.Prop.SITE, ValueConverter::getString).flatMap(siteName ->
                        node.getParentOptional().flatMap(parent -> parent.getProperty(DictionaryConfiguration.Prop.NAME, ValueConverter::getString)).map(labelName ->
                                labelName + " - " + siteName
                        )
                );
            }
            return node.getProperty(DictionaryConfiguration.Prop.NAME, ValueConverter::getString);
        }
    }
}
