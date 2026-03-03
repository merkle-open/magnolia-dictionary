package com.merkle.oss.magnolia.dictionary.actions.label;

import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.detail.action.SaveDetailSubAppAction;
import info.magnolia.ui.contentapp.detail.action.SaveDetailSubAppActionDefinition;
import info.magnolia.ui.datasource.ItemResolver;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.observation.DatasourceObservation;

import javax.jcr.Node;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.inject.Inject;

/*
 * Used to save name and value into the jcr of the siteSpecificLabel node to be able to find the siteSpecificLabel with the search (only jcr props can be searched, value provider do not work!)
 */
public class EditLabelSaveAction extends SaveDetailSubAppAction<Node> {
    private final PowerNodeService powerNodeService;

    @Inject
    public EditLabelSaveAction(
            final SaveDetailSubAppActionDefinition definition,
            final CloseHandler closeHandler,
            final ValueContext<Node> valueContext,
            final EditorView<Node> form,
            final Datasource<Node> datasource,
            final DatasourceObservation.Manual datasourceObservation,
            final LocationController locationController,
            final AppContext appContext,
            final ItemResolver<Node> itemResolver,
            final PowerNodeService powerNodeService
    ) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation, locationController, appContext, itemResolver);
        this.powerNodeService = powerNodeService;
    }

    @Override
    protected void write() {
        try {
            final PowerNode node = powerNodeService.convertToPowerNode(getValueContext().getSingleOrThrow());
            setPropertyOrThrow(node, DictionaryConfiguration.Prop.NAME);
            setPropertyOrThrow(node, DictionaryConfiguration.Prop.VALUE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.write();
    }

    private void setPropertyOrThrow(final PowerNode node, final String propName) {
        final String propValue = (String) getForm().getPropertyValue(propName).orElseThrow();
        node.setProperty(propName, propValue, ValueConverter::toValue);
    }

    public static class Definition extends SaveDetailSubAppActionDefinition {
        public Definition() {
            setImplementationClass(EditLabelSaveAction.class);
        }
    }
}
