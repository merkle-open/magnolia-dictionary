package com.merkle.oss.magnolia.dictionary.workbench.column;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Item;
import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Inject;

public class MultiColumnValueProvider implements com.vaadin.data.ValueProvider<Item, String> {
	private static final String EMPTY_PLACEHOLDER = "-";
	private static final String SEPARATOR = "|";
    private final PowerNodeService powerNodeService;
    private final LocaleUtil localeUtils;
    private final SiteProvider siteProvider;

    @Inject
    public MultiColumnValueProvider(
            final PowerNodeService powerNodeService,
            final LocaleUtil localeUtils,
            final SiteProvider siteProvider
    ) {
        this.powerNodeService = powerNodeService;
        this.localeUtils = localeUtils;
        this.siteProvider = siteProvider;
    }

    @Override
    public String apply(final Item item) {
        return Optional
                .ofNullable(item)
                .filter(Item::isNode)
                .map(i -> (Node)i)
                .map(powerNodeService::convertToPowerNode)
                .map(this::getName)
                .orElse("");
    }

    private String getName(final PowerNode node) {
        return siteProvider.getGenericSite().getI18n().getLocales().stream()
                .sorted(Comparator.comparing(Locale::getLanguage))
                .map(localeUtils::toLocaleString)
                .map(localePropName ->
                        node.getProperty(localePropName, ValueConverter::getString).orElse(EMPTY_PLACEHOLDER)
                )
                .collect(Collectors.joining(SEPARATOR));
    }
}
