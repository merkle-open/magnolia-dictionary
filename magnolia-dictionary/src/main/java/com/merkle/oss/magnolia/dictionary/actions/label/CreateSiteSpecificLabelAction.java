package com.merkle.oss.magnolia.dictionary.actions.label;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.contentapp.detail.ContentDetailSubApp;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;

import javax.jcr.Node;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

import jakarta.inject.Inject;

public class CreateSiteSpecificLabelAction extends CommitAction<Node> {
    private final ComponentProvider componentProvider;

    @Inject
	public CreateSiteSpecificLabelAction(
            final Definition definition,
            final CloseHandler closeHandler,
            final ValueContext<Node> valueContext,
            final EditorView<Node> form,
            final Datasource<Node> datasource,
            final DatasourceObservation.Manual<Node> datasourceObservation,
            final ComponentProvider componentProvider
    ) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        this.componentProvider = componentProvider;
    }

    @Override
    public void execute() {
        if (validateForm()) {
            Exceptions.wrap().run(() -> {
                final Node parent = getValueContext().getSingle().orElseThrow(() ->
                    new NullPointerException("parent node not present!")
                );
                final Option siteOption = (Option)getForm().getPropertyValue("site").orElseThrow(() ->
                        new NullPointerException("site not present!")
                );
                /*
                 * node name can contain illegal chars. This is necessary to transmit the site name (which can contain illegal node name chars).
                 * The node is not persisted here and only used to pass via url param to com.merkle.oss.magnolia.dictionary.field.LabelJcrNodeFromLocationProvider
                 */
                final Node node = NodeUtil.createPath(parent, siteOption.getValue(), DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE);
                getValueContext().set(node);

                final EditLabelAction.Definition editLabelAction = new EditLabelAction.Definition(ContentDetailSubApp.VIEW_TYPE_ADD);
                componentProvider.newInstance(editLabelAction.getImplementationClass(), editLabelAction, getValueContext()).execute();
            });
        }
    }

    @Override
    protected FormView<Node> getForm() {
        return (FormView<Node>) super.getForm();
    }

    public static class Definition extends CommitActionDefinition {
        public Definition() {
            setImplementationClass(CreateSiteSpecificLabelAction.class);
            setAvailability(new ConfiguredAvailabilityDefinition());
        }
    }
}
