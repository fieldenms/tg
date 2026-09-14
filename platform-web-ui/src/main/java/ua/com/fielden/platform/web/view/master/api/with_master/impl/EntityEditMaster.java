package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Custom entity master for all edit actions.
 * <p>
 * Firstly, it ensures that it will not be closed on SAVE / CANCEL buttons of embedded master (see {@link #shouldCloseAfterSave()}).
 * Secondly, it adds support for navigation between heterogenic entities. Use {@link EntityNavigationPreAction} to enable navigation.
 *
 * @author TG Team
 *
 */
public class EntityEditMaster extends EntityManipulationMaster<EntityEditAction> {

    private final IRenderable renderable;

    public EntityEditMaster(final Class<EntityEditAction> entityType, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, shouldRefreshParentCentreAfterSave);
        final String masterTemplate = super.render().render().toString().replace("//@master-is-ready-custom-code",
                "             //Report a failure to retrieve the entity being navigated to\n"
              + "             self._handleBindingEntityRetrievedError = function (e) {\n"
              + "                 this.fire('tg-master-navigation-error');\n"
              + "             }.bind(self);\n"
              + "             self.$.loader.addEventListener('binding-entity-retrieved-error', self._handleBindingEntityRetrievedError);\n");
      this.renderable = () -> new InnerTextElement(masterTemplate);
    }

    /**
     * Closing of the enclosing dialog is governed by this master, and not by the SAVE action of the embedded master.
     */
    @Override
    protected boolean shouldCloseAfterSave() {
        return false;
    }

    @Override
    public IRenderable render() {
        return renderable;
    }
}
