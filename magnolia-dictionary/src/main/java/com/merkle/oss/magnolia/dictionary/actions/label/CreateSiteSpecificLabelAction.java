package com.merkle.oss.magnolia.dictionary.actions.label;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionDefinition;
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
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;

import jakarta.inject.Inject;

public class CreateSiteSpecificLabelAction extends CommitAction<Node> {
    private final ComponentProvider componentProvider;
    private final PowerNodeService powerNodeService;

    @Inject
	public CreateSiteSpecificLabelAction(
            final Definition definition,
            final CloseHandler closeHandler,
            final ValueContext<Node> valueContext,
            final EditorView<Node> form,
            final Datasource<Node> datasource,
            final DatasourceObservation.Manual<Node> datasourceObservation,
            final ComponentProvider componentProvider,
            final PowerNodeService powerNodeService
    ) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        this.componentProvider = componentProvider;
        this.powerNodeService = powerNodeService;
    }

    @Override
    protected void write() {
        Exceptions.wrap().run(() -> {
            final Node parent = getValueContext().getSingle().orElseGet(getDatasource()::getRoot);
            final Option siteOption = (Option)getForm().getPropertyValue("site").orElseThrow(() ->
                    new NullPointerException("site not present!")
            );
            final String nodeName = siteOption.getValue();
            /*
             * node name can contain illegal chars. This is necessary to transmit the site name (which can contain illegal node name chars).
             * The node is not persisted here and only used to pass via url param to com.merkle.oss.magnolia.dictionary.field.LabelJcrNodeFromLocationProvider
             */
            final Node node = NodeUtil.createPath(parent, nodeName, DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE);

            getForm().write(node);
            getDatasource().save(node);
            getValueContext().set(node);
            if (ActionDefinition.RefreshBehavior.ITEMS.equals(getDefinition().getDatasourceRefreshBehavior())) {
                getDatasourceObservation().trigger(node);
            } else {
                getDatasourceObservation().trigger();
            }

            final EditLabelAction.Definition editLabelAction = new EditLabelAction.Definition(ContentDetailSubApp.VIEW_TYPE_ADD);
            componentProvider.newInstance(editLabelAction.getImplementationClass(), editLabelAction, getValueContext()).execute();
        });
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
