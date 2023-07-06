package com.namics.oss.magnolia.dictionary.i18nsystem;

import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;

import java.util.Collection;

public interface I18nResourcesProvider {
	Collection<Resource> getI18nResources(ResourceOrigin resourceOrigin);
}
