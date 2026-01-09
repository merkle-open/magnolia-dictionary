package com.merkle.oss.magnolia.dictionary.actions.label;

import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.contentapp.action.OpenDetailSubappActionDefinition;
import info.magnolia.ui.contentapp.detail.ContentDetailSubApp;

import java.util.Set;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

public abstract class EditLabelAction {
    public static class Definition extends OpenDetailSubappActionDefinition {
        public Definition() {
            this(ContentDetailSubApp.VIEW_TYPE_EDIT);
        }
        public Definition(final String viewType) {
            setName("editLabel");
            setIcon(MagnoliaIcons.EDIT.getCssClass());
            setAppName(DictionaryConfiguration.APP_NAME);
            setSubAppName(DictionaryConfiguration.EDIT_SUB_APP_NAME);
            setViewType(viewType);
            final ConfiguredAvailabilityDefinition availability = new ConfiguredAvailabilityDefinition();
            availability.setWritePermissionRequired(true);
            availability.setNodeTypes(Set.of(
                    DictionaryConfiguration.LABEL_NODE_TYPE,
                    DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE
            ));
            setAvailability(availability);
        }
    }
}
