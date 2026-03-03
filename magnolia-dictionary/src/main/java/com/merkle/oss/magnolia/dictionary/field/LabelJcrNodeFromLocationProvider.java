package com.merkle.oss.magnolia.dictionary.field;

import static info.magnolia.ui.contentapp.detail.ContentDetailSubApp.*;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.ui.contentapp.detail.ContentDetailSubApp;
import info.magnolia.ui.contentapp.version.VersionResolver;
import info.magnolia.ui.datasource.ItemResolver;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.editor.ItemProviderStrategy;
import info.magnolia.ui.editor.JcrNodeProviderDefinition;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Inject;

public class LabelJcrNodeFromLocationProvider implements ItemProviderStrategy<Node, ContentDetailSubApp.DetailLocation> {
    private final Pattern PATH_PATTERN = Pattern.compile("(.*)/([^/]*)$");
    private final ItemResolver<Node> itemResolver;
    private final VersionResolver<Node> versionResolver;
    private final NodeNameHelper nodeNameHelper;
    private final PowerNodeService powerNodeService;
    private final JcrDatasource datasource;

    @Inject
    public LabelJcrNodeFromLocationProvider(
            final JcrDatasource datasource,
            final ItemResolver<Node> itemResolver,
            final VersionResolver<Node> versionResolver,
            final NodeNameHelper nodeNameHelper,
            final PowerNodeService powerNodeService
    ) {
        this.datasource = datasource;
        this.itemResolver = itemResolver;
        this.versionResolver = versionResolver;
        this.nodeNameHelper = nodeNameHelper;
        this.powerNodeService = powerNodeService;
    }

    @Override
    public Optional<Node> read(final ContentDetailSubApp.DetailLocation location) {
        final String nodePath = location.getNodePath();
        final String viewType = location.getViewType();

        return switch (viewType) {
            case VIEW_TYPE_ADD -> Optional.ofNullable(createSiteSpecificLabelNode(nodePath));
            case VIEW_TYPE_VERSION -> versionResolver.getVersion(nodePath, location.getVersion());
            default -> itemResolver.getItemById(nodePath);
        };
    }

    private Node createSiteSpecificLabelNode(final String nodePath) {
        try {
            final Matcher matcher = PATH_PATTERN.matcher(nodePath);
            if(!matcher.matches()) {
                throw new IllegalArgumentException("node path is invalid! "+ nodePath);
            }
            final PowerNode parent = powerNodeService.convertToPowerNode(datasource.getJCRSession().getNode(matcher.group(1)));
            final String siteName = matcher.group(2);
            final String name = nodeNameHelper.getValidatedName(siteName);
            final PowerNode siteSpecificLabelNode = parent.getOrAddChild(name, DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE);
            siteSpecificLabelNode.setProperty(DictionaryConfiguration.Prop.SITE, siteName, ValueConverter::toValue);
            return siteSpecificLabelNode;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public static class Definition extends JcrNodeProviderDefinition<ContentDetailSubApp.DetailLocation> {
        public Definition() {
            setImplementationClass(LabelJcrNodeFromLocationProvider.class);
        }
    }
}
