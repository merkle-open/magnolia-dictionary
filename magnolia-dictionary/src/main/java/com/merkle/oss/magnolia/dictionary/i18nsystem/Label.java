package com.merkle.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.Lazy;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

public record Label(String key, String siteName, String defaultValue, Map<Locale, String> values) {

    @Singleton
    public static class Factory {
        private final Provider<String[]> availableLanguagesProvider;
        private final LocaleUtil localeUtils;

        @Inject
        public Factory(
                final SiteProvider siteProvider,
                final LocaleUtil localeUtils
        ) {
            availableLanguagesProvider = Lazy.of(() ->
                    siteProvider.getGenericSite().getI18n().getLocales().stream().map(localeUtils::toLocaleString).toArray(String[]::new)
            )::get;
            this.localeUtils = localeUtils;
        }

        public Optional<Label> create(final Node node) {
            return Optional.ofNullable(PropertyUtil.getString(node, DictionaryConfiguration.Prop.NAME, null)).flatMap(key ->
                    Optional.ofNullable(PropertyUtil.getString(node, DictionaryConfiguration.Prop.SITE, null)).flatMap(siteName ->
                            Optional.ofNullable(PropertyUtil.getString(node, DictionaryConfiguration.Prop.VALUE, null)).map(defautValue ->
                                    new Label(key, siteName, defautValue, getValues(node))
                            )
                    )
            );
        }

        private Map<Locale, String> getValues(final Node node) {
            try {
                final Iterator<Property> properties = node.getProperties(availableLanguagesProvider.get());
                return StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(properties, Spliterator.CONCURRENT),true)
                        .collect(Collectors.toMap(
                                property -> localeUtils.fromLocaleString(Exceptions.wrap().get(property::getName)),
                                property -> Exceptions.wrap().get(property::getString)
                        ));
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Persistor {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private final NodeNameHelper nodeNameHelper;
        private final LocaleUtil localeUtils;

        @Inject
        public Persistor(
                final NodeNameHelper nodeNameHelper,
                final LocaleUtil localeUtils
        ) {
            this.nodeNameHelper = nodeNameHelper;
            this.localeUtils = localeUtils;
        }

        public void persist(final Node dictionaryRootNode, final Label label) {
            persist(dictionaryRootNode, label, false);
        }
        public void persist(final Node dictionaryRootNode, final Label label, final boolean isInstall) {
            try {
                final Node labelNode = getLabelNode(dictionaryRootNode, label);
                if (isInstall) {
                    PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.NAME, label.key());
                    PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.VALUE, label.defaultValue());
                    PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.SITE, label.siteName());
                    PropertyUtil.setProperty(labelNode, DictionaryConfiguration.Prop.EXPIRED, null);
                }
                for(Map.Entry<Locale, String> entry :label.values().entrySet()) {
                    PropertyUtil.setProperty(
                            labelNode,
                            localeUtils.toLocaleString(entry.getKey()),
                            Optional.of(entry.getValue()).filter(StringUtils::isNotEmpty).orElse(null)
                    );
                }
            } catch (RepositoryException e) {
                LOG.warn("Could not create label: '{}'", label, e);
            }
        }

        private Node getLabelNode(final Node dictionaryRootNode, final Label label) throws RepositoryException {
            final String labelNodeName = createValidNodeName(label.key());
            final Node labelNode = NodeUtil.createPath(dictionaryRootNode, labelNodeName, DictionaryConfiguration.LABEL_NODE_TYPE);
            if(Objects.equals(label.siteName(), SiteProvider.GENERIC_SITE_NAME)) {
                return labelNode;
            }
            final String siteSpecificLabelNodeName = nodeNameHelper.getValidatedName(label.siteName());
            return NodeUtil.createPath(labelNode, siteSpecificLabelNodeName, DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE);
        }

        private String createValidNodeName(final String nodeName) {
            return StringUtils.replaceChars(nodeName, "%/:[]*'\"|\t\r\n", "_");
        }
    }
}
