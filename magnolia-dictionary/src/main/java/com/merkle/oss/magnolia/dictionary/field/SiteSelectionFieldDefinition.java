package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.DatasourceDefinition;
import info.magnolia.ui.field.ComboBoxFieldDefinition;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

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

    private Set<String> getSiteNames(final Node labelNode) {
        try {
            return StreamSupport
                    .stream(NodeUtil.collectAllChildren(labelNode, new NodeTypePredicate(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE)).spliterator(), false)
                    .map(NodeUtil::getName)
                    .collect(Collectors.toSet());
        } catch (RepositoryException e) {
            return Collections.emptySet();
        }
    }
}
