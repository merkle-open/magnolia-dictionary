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
import javax.jcr.RepositoryException;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

import jakarta.inject.Inject;

/*
 * Used to save name and value into the jcr of the siteSpecificLabel node to be able to find the siteSpecificLabel with the search (only jcr props can be searched, value provider do not work!)
 */
public class EditLabelSaveAction extends SaveDetailSubAppAction<Node> {

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
            final ItemResolver<Node> itemResolver
    ) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation, locationController, appContext, itemResolver);
    }

    @Override
    protected void write() {
        try {
            final Node node = getValueContext().getSingleOrThrow();
            setPropertyOrThrow(node, DictionaryConfiguration.Prop.NAME);
            setPropertyOrThrow(node, DictionaryConfiguration.Prop.VALUE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.write();
    }

    private void setPropertyOrThrow(final Node node, final String propName) throws RepositoryException {
        final String propValue = (String) getForm().getPropertyValue(propName).orElseThrow();
        node.setProperty(propName, propValue);
    }

    public static class Definition extends SaveDetailSubAppActionDefinition {
        public Definition() {
            setImplementationClass(EditLabelSaveAction.class);
        }
    }
}
