package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.JcrNodeResolver;
import info.magnolia.ui.contentapp.detail.ContentDetailSubApp;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;

import java.util.Optional;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;

import jakarta.inject.Inject;

public class LocationBasedNodeProvider {
    private final ComponentProvider componentProvider;
    private final PowerNodeService powerNodeService;

    @Inject
    public LocationBasedNodeProvider(
            final ComponentProvider componentProvider,
            final PowerNodeService powerNodeService
    ) {
        this.componentProvider = componentProvider;
        this.powerNodeService = powerNodeService;
    }

    public Optional<PowerNode> getNode() {
        return Optional
                .of(Components.getComponent(LocationController.class).getWhere())
                .map(ContentDetailSubApp.DetailLocation::wrap)
                .map(ContentDetailSubApp.DetailLocation::getNodePath)
                .flatMap(this::getNode);
    }

    private Optional<PowerNode> getNode(final String path) {
        try {
            final JcrDatasourceDefinition datasourceDefinition = new JcrDatasourceDefinition();
            datasourceDefinition.setWorkspace(DictionaryConfiguration.REPOSITORY);
            final JcrDatasource jcrDatasource = componentProvider.newInstance(JcrDatasource.class, datasourceDefinition);
            final JcrNodeResolver jcrNodeResolver = componentProvider.newInstance(JcrNodeResolver.class, jcrDatasource);
            return jcrNodeResolver.getItemById(path)
                    .filter(Item::isNode)
                    .map(Node.class::cast)
                    .map(powerNodeService::convertToPowerNode);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
