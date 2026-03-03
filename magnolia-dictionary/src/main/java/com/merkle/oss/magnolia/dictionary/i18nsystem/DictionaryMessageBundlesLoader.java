package com.merkle.oss.magnolia.dictionary.i18nsystem;

import static javax.jcr.query.Query.JCR_SQL2;

import info.magnolia.context.SystemContext;
import info.magnolia.module.site.Site;

import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Provider;

@Singleton
public class DictionaryMessageBundlesLoader implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryMessageBundlesLoader.class);
    private final SiteProvider siteProvider;
    private final LocaleUtil localeUtils;
    private final Provider<SystemContext> systemContextProvider;
    private final PowerNodeService powerNodeService;
    private Map<String, Map<Locale, Properties>> messages;

    @Inject
    public DictionaryMessageBundlesLoader(
            final SiteProvider siteProvider,
            final LocaleUtil localeUtils,
            final Provider<SystemContext> systemContextProvider,
            final PowerNodeService powerNodeService
    ) {
        this.siteProvider = siteProvider;
        this.localeUtils = localeUtils;
        this.systemContextProvider = systemContextProvider;
        this.powerNodeService = powerNodeService;
    }

    public Map<Locale, Properties> getGenericMessages() {
        return getMessages(SiteProvider.GENERIC_SITE_NAME);
    }
    public Map<Locale, Properties> getMessages(final Site site) {
        return getMessages(site.getName());
    }
    private Map<Locale, Properties> getMessages(final String siteName) {
        if(messages == null) {
            reload();
        }
        return messages.getOrDefault(siteName, Collections.emptyMap());
    }

    public void reload() {
        Exceptions.wrap().run(() -> {
            final Session session = systemContextProvider.get().getJCRSession(DictionaryConfiguration.REPOSITORY);
            loadMessages(session);
            session.logout();
        });
    }
    private void loadMessages(final Session session) {
        final PowerNode root = powerNodeService.getRootNode(session);
        messages = getProperties(root);
    }

    private Map<String, Map<Locale, Properties>> getProperties(final PowerNode root) {
        LOG.debug("Loading dictionary properties...");
        return streamMessages(root).collect(Collectors.groupingBy(
                message -> message.site().getName(),
                Collectors.groupingBy(
                        Message::locale,
                        Collectors.toMap(
                                Message::key,
                                Message::value,
                                (first, second) -> first,
                                Properties::new
                        )
                )
        ));
    }

    private Stream<Message> streamMessages(final PowerNode root) {
        final Set<Site> sites = siteProvider.streamAllSites().collect(Collectors.toSet());
        return root.streamChildren().flatMap(labelNode ->
            labelNode.getProperty(DictionaryConfiguration.Prop.NAME, ValueConverter::getString).stream().flatMap(messageKey ->
                    sites.stream().flatMap(site ->
                            streamMessageValues(site, labelNode).map(entry ->
                                    new Message(messageKey, site, entry.getKey(), entry.getValue())
                            )
                    )
            )
        );
    }

    private Stream<Map.Entry<Locale, String>> streamMessageValues(final Site site, final PowerNode messageRootNode) {
        return getMessageNode(site, messageRootNode)
                .stream()
                .flatMap(messageNode ->
                        site.getI18n().getLocales().stream().map(locale ->
                            messageNode.getProperty(localeUtils.toLocaleString(locale), ValueConverter::getString).map(value ->
                                Map.entry(locale, value)
                            )
                        )
                )
                .flatMap(Optional::stream);
    }
    private Optional<PowerNode> getMessageNode(final Site site, final PowerNode messageRootNode) {
        if(SiteProvider.GENERIC_SITE_NAME.equals(site.getName())) {
            return Optional.of(messageRootNode);
        }
        return getSiteMessageNode(messageRootNode, site);
    }

    private Optional<PowerNode> getSiteMessageNode(final PowerNode messageRootNode, final Site site) {
        try {
            final Session jcrSession = messageRootNode.getSession();
            final String statement = String.format("SELECT * FROM [nt:base] AS node WHERE ISDESCENDANTNODE(node, '%s') AND [%s]='%s'", messageRootNode.getPath(), DictionaryConfiguration.Prop.SITE, site.getName());
            final Query query = jcrSession.getWorkspace().getQueryManager().createQuery(statement, JCR_SQL2);

            final Iterator<Node> nodes = query.execute().getNodes();
            return StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(nodes, Spliterator.ORDERED),false)
                    .findFirst()
                    .map(powerNodeService::convertToPowerNode);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void onEvent(EventIterator events) {
        if (events.getSize() > 0) {
            reload();
        }
    }

    record Message(String key, Site site, Locale locale, String value){}
}
