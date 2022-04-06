package com.namics.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.util.FileResourceCollectorVisitor;
import info.magnolia.resourceloader.util.Functions;

import java.util.Collection;
import java.util.function.Predicate;

public class DefaultI18nResourcesProvider implements I18nResourcesProvider {
	private static final Predicate<Resource> DIRECTORY_PREDICATE = Functions.pathStartsWith("/");
	private static final Predicate<Resource> RESOURCE_PREDICATE = Functions.pathMatches("(/*/i18n/.*dictionary-messages.*\\.properties|/mgnl-i18n/.*dictionary-messages.*\\.properties)");

	@Override
	public Collection<Resource> getI18nResources(final ResourceOrigin resourceOrigin) {
		final FileResourceCollectorVisitor visitor = FileResourceCollectorVisitor.on(DIRECTORY_PREDICATE, RESOURCE_PREDICATE);
		resourceOrigin.traverseWith(visitor);
		return visitor.getCollectedResources();
	}
}