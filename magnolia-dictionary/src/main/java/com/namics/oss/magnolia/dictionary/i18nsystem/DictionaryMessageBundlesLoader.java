package com.namics.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.machinezoo.noexception.Exceptions;
import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.util.LocaleUtils;
import com.namics.oss.magnolia.dictionary.util.NodeUtil;

@Singleton
public class DictionaryMessageBundlesLoader implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryMessageBundlesLoader.class);
    private final Provider<SystemContext> systemContextProvider;
    private Map<Locale, Properties> messages = Collections.emptyMap();

    @Inject
    public DictionaryMessageBundlesLoader(final Provider<SystemContext> systemContextProvider) {
        this.systemContextProvider = systemContextProvider;
    }

    public void reload() {
        Exceptions.wrap().run(() -> {
            final Session session = systemContextProvider.get().getJCRSession(DictionaryConfiguration.REPOSITORY);
            loadMessages(session);
            session.logout();
        });
    }

    private void loadMessages(final Session session) throws RepositoryException {
        final Node root = session.getRootNode();
        messages = LocaleUtils.getLocalesOfAllSiteDefinitions().stream().collect(Collectors.toMap(
                Function.identity(),
                locale -> getProperties(locale, root))
        );
    }

    private Properties getProperties(final Locale locale, final Node root) {
        LOG.debug("Loading dictionary properties with locale [{}]...", locale);
        final Properties properties = new Properties();
        streamMessages(locale, root).forEach(entry ->
                properties.put(entry.getKey(), entry.getValue())
        );
        return properties;
    }

    private Stream<Map.Entry<String, String>> streamMessages(final Locale locale, final Node root) {
        return StreamSupport
                .stream(
                        Spliterators.spliteratorUnknownSize(Exceptions.wrap().get(() -> NodeUtil.getNodes(root)).iterator(), Spliterator.ORDERED),
                        false
                )
                .map(message ->
                        Optional.ofNullable(PropertyUtil.getString(message, LocaleUtils.getLocaleString(locale))).map(value ->
                                Map.entry(NodeUtil.getName(message), value)
                        )
                )
                .flatMap(Optional::stream);
    }

    public Map<Locale, Properties> getMessages() {
        return messages;
    }

    @Override
    public void onEvent(EventIterator events) {
        if (events.getSize() > 0) {
            reload();
        }
    }
}
