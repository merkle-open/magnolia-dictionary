package com.merkle.oss.magnolia.dictionary.workbench.column;

import info.magnolia.jcr.util.PropertyUtil;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;

import jakarta.inject.Inject;

public class MultiColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
	private static final String EMPTY_PLACEHOLDER = "-";
	private static final String SEPARATOR = "|";
    private final LocaleUtil localeUtils;
    private final SiteProvider siteProvider;

    @Inject
    public MultiColumnValueProvider(
            final LocaleUtil localeUtils,
            final SiteProvider siteProvider
    ) {
        this.localeUtils = localeUtils;
        this.siteProvider = siteProvider;
    }

    @Override
    public String apply(final Item item) {
        if (item != null && item.isNode()) {
            final Node node = (Node) item;
            return siteProvider.getGenericSite().getI18n().getLocales().stream()
                    .sorted(Comparator.comparing(Locale::getLanguage))
                    .map(localeUtils::toLocaleString)
                    .map(localePropName ->
                        PropertyUtil.getString(node, localePropName, EMPTY_PLACEHOLDER)
                    )
                    .collect(Collectors.joining(SEPARATOR));
        }
        return "";
    }
}
