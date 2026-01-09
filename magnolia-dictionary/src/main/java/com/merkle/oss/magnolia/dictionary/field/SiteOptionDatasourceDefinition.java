package com.merkle.oss.magnolia.dictionary.field;

import info.magnolia.module.site.Site;
import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import info.magnolia.ui.filter.DataFilter;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.provider.AbstractDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.ItemCaptionGenerator;

import jakarta.inject.Inject;

@DatasourceType("siteOptionDatasource")
public class SiteOptionDatasourceDefinition extends OptionListDefinition {
	private boolean includeGeneric;
	private Set<String> excludedSiteNames = Collections.emptySet();

	@Override
	public String getName() {
		return "siteOption";
	}

	public Set<String> getExcludedSiteNames() {
		return excludedSiteNames;
	}

	public void setExcludedSiteNames(final Set<String> excludedSiteNames) {
		this.excludedSiteNames = excludedSiteNames;
	}

	public boolean isIncludeGeneric() {
		return includeGeneric;
	}

	public void setIncludeGeneric(boolean includeGeneric) {
		this.includeGeneric = includeGeneric;
	}

	public static class SelectFieldSupport implements info.magnolia.ui.field.SelectFieldSupport<Option> {
        private final SiteOptionDatasourceDefinition siteOptionDatasourceDefinition;
        private final SiteProvider siteProvider;

        @Inject
		public SelectFieldSupport(
				final SiteOptionDatasourceDefinition siteOptionDatasourceDefinition,
				final SiteProvider siteProvider
		) {
            this.siteOptionDatasourceDefinition = siteOptionDatasourceDefinition;
            this.siteProvider = siteProvider;
        }

		@Override
		public DataProvider<Option, DataFilter> getDataProvider() {
			return new SiteDataProvider(siteProvider, siteOptionDatasourceDefinition);
		}

		@Override
		public ItemCaptionGenerator<Option> getItemCaptionGenerator() {
			return option -> StringUtils.defaultIfBlank(option.getLabel(), option.getName());
		}

		@Override
		public Converter<Option, String> defaultConverter() {
			return new Converter<>() {
				@Override
				public Result<String> convertToModel(final Option value, final com.vaadin.data.ValueContext context) {
					return Result.ok(Optional.ofNullable(value).map(Option::getValue).orElse(null));
				}
				@Override
				public Option convertToPresentation(final String value, final com.vaadin.data.ValueContext context) {
					return Optional.ofNullable(value).map(v -> SiteDataProvider.createOption(v, v)).orElse(null);
				}
			};
		}

		private static class SiteDataProvider extends AbstractDataProvider<Option, DataFilter> {
			private final SiteProvider siteProvider;
            private final SiteOptionDatasourceDefinition siteOptionDatasourceDefinition;

            public SiteDataProvider(final SiteProvider siteProvider, final SiteOptionDatasourceDefinition siteOptionDatasourceDefinition) {
				this.siteProvider = siteProvider;
                this.siteOptionDatasourceDefinition = siteOptionDatasourceDefinition;
            }

			@Override
			public boolean isInMemory() {
				return true;
			}
			@Override
			public int size(final Query<Option, DataFilter> query) {
				return (int) fetch(query).count();
			}
			@Override
			public Stream<Option> fetch(final Query<Option, DataFilter> query) {
				return siteProvider
						.streamAllSites(siteOptionDatasourceDefinition.isIncludeGeneric())
						.filter(site -> !siteOptionDatasourceDefinition.getExcludedSiteNames().contains(site.getName()))
						.filter(site -> query.getFilter().map(filter -> applyFilter(site, filter)).orElse(true))
						.map(SiteDataProvider::createOption);
			}

			private boolean applyFilter(final Site site, final DataFilter filter) {
				if(filter.getPropertyFilters().containsKey(DictionaryConfiguration.Prop.SITE)) {
					final Set<String> sitesFilter = (Set<String>) filter.getPropertyFilters().get(DictionaryConfiguration.Prop.SITE);
					return sitesFilter.contains(site.getName());
				}
				return true;
			}

			private static Option createOption(final Site site) {
				return createOption(site.getName(), site.getName());
			}
			private static Option createOption(final String label, final String value) {
				final Option option = new Option();
				option.setLabel(label);
				option.setName(value);
				option.setValue(value);
				return option;
			}
		}
	}
}
