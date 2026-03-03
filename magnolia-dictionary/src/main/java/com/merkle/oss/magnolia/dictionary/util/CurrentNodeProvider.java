package com.merkle.oss.magnolia.dictionary.util;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.framework.ioc.BeanStore;
import info.magnolia.ui.framework.ioc.SessionStore;
import info.magnolia.ui.framework.ioc.UiContextReference;

import java.util.Optional;

import javax.jcr.Node;

import jakarta.inject.Provider;

public interface CurrentNodeProvider extends Provider<Optional<Node>> {
    class SubAppContextBeanStoreCurrentNodeProvider implements CurrentNodeProvider {

        @Override
        public Optional<Node> get() {
            return getValueContext().flatMap(ValueContext::getSingle);
        }

        protected Optional<ValueContext<Node>> getValueContext() {
            return getBeanStore().map(beanStore -> beanStore.get(ValueContext.class));
        }

        protected Optional<BeanStore> getBeanStore() {
            try {
                return Components.getComponent(AppController.class)
                        .getCurrentAppContext()
                        .map(AppContext::getActiveSubAppContext)
                        .map(activeSubAppContext -> SessionStore.access().getBeanStore(UiContextReference.ofSubApp(activeSubAppContext)));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
