package com.merkle.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.context.SystemContext;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.dictionary.util.predicates.IsLabelNamesAnyOfPredicate;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class DictionaryMessageBundlesInstaller {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final I18nResourcesProvider i18nResourcesProvider;
    private final ResourceOrigin<?> resourceOrigin;
	private final Provider<SystemContext> systemContextProvider;
    private final Label.Persistor labelPersistor;
    private final PowerNodeService powerNodeService;

    @Inject
	public DictionaryMessageBundlesInstaller(
			final I18nResourcesProvider i18nResourcesProvider,
			final ResourceOrigin resourceOrigin,
			final Provider<SystemContext> systemContextProvider,
			final Label.Persistor labelPersistor,
			final PowerNodeService powerNodeService
	) {
        this.i18nResourcesProvider = i18nResourcesProvider;
        this.resourceOrigin = resourceOrigin;
		this.systemContextProvider = systemContextProvider;
        this.labelPersistor = labelPersistor;
        this.powerNodeService = powerNodeService;
    }

	public void loadLabelsToDictionary() throws RepositoryException {
		final Session session = systemContextProvider.get().getJCRSession(DictionaryConfiguration.REPOSITORY);
		final PowerNode dictionaryRoot = powerNodeService.getRootNode(session);

		final Set<String> notExpired = new HashSet<>();
		streamPropertyEntries().forEach(message -> {
			final String messageValue = message.getValue().toString();
			final String messageName = message.getKey().toString();
			labelPersistor.persist(dictionaryRoot, new Label(messageName, SiteProvider.GENERIC_SITE_NAME, messageValue, Collections.emptyMap()), true);
			// collect list of messages/labels present in the property files
			notExpired.add(messageName);
		});

		markExpiredProperties(dictionaryRoot, notExpired);
		session.save();
	}

	private void markExpiredProperties(final PowerNode dictionaryRoot, final Set<String> notExpired) {
		dictionaryRoot.streamChildren(new IsLabelNamesAnyOfPredicate(notExpired).negate()).forEach(expiredNode ->
			expiredNode.setProperty(DictionaryConfiguration.Prop.EXPIRED, true, ValueConverter::toValue)
		);
	}

	private Stream<Map.Entry<Object, Object>> streamPropertyEntries() {
		final Collection<Resource> resources = i18nResourcesProvider.getI18nResources(resourceOrigin);
		return resources.stream()
				.map(this::loadProperties)
				.flatMap(properties ->
						properties.entrySet().stream()
				);
	}

	private Properties loadProperties(final Resource resource) {
		final Properties properties = new Properties();
		try (InputStream in = resource.openStream()) {
			LOG.debug("Loading properties from '{}'", resource);
			final Reader inStream = new InputStreamReader(BOMInputStream.builder().setInputStream(in).get(), StandardCharsets.UTF_8);
			properties.load(inStream);
		} catch (Exception e) {
			LOG.error("Failed to read properties from '{}', skipping properties file...", resource, e);
		}
		return properties;
	}
}
