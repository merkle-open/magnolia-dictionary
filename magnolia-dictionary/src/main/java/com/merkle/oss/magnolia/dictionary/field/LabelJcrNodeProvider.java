package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.ui.editor.ItemProviderStrategy;
import info.magnolia.ui.editor.JcrNodeProviderDefinition;

import java.util.Optional;

import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

public class LabelJcrNodeProvider implements ItemProviderStrategy<Node, Node> {
    @Override
    public Optional<Node> read(final Node reference) {
        try {
            if (reference.isNodeType(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE)) {
                return Optional.of(reference.getParent());
            }
            return Optional.of(reference);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Definition extends JcrNodeProviderDefinition<Node> {
        public Definition() {
            setImplementationClass(LabelJcrNodeProvider.class);
        }
    }
}
