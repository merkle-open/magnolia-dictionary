package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.DatasourceDefinition;
import info.magnolia.ui.field.ComboBoxFieldDefinition;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;

public class SiteSelectionFieldDefinition<T> extends ComboBoxFieldDefinition<T> {
    private final LocationBasedNodeProvider locationBasedNodeProvider;

    public SiteSelectionFieldDefinition() {
        this.locationBasedNodeProvider = Components.getComponent(LocationBasedNodeProvider.class);
    }

    @Override
    public DatasourceDefinition getDatasource() {
        final SiteOptionDatasourceDefinition siteOptions = new SiteOptionDatasourceDefinition();
        final Set<String> alreadyMaintainedSiteNames = locationBasedNodeProvider.getNode().map(this::getSiteNames).orElseGet(Collections::emptySet);
        siteOptions.setExcludedSiteNames(alreadyMaintainedSiteNames);
        return siteOptions;
    }

    private Set<String> getSiteNames(final PowerNode labelNode) {
        return labelNode
                .streamChildren(new IsPrimaryNodeType<>(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE))
                .map(PowerNode::getName)
                .collect(Collectors.toSet());
    }
}
