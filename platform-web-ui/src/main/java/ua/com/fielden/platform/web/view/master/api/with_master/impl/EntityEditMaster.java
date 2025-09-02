package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Custom entity master for all edit actions.
 * <p>
 * Firstly, it ensures that it will not be closed on SAVE / CANCEL buttons of embedded master (see closeAfterExecution property setting).
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
                "             //Provide custom after load listener\n"
              + "             self._seqEditAfterLoadListener = function (e) {\n"
              + "                 this._assignPostSavedHandlersForEmbeddedMaster(e);\n"
              + "                 const saveButton = e.detail.shadowRoot.querySelector(\"tg-action[role='save']\");\n"
              + "                 if (saveButton) {\n"
              + "                     saveButton.closeAfterExecution = false;\n"
              + "                 }\n"
              + "             }.bind(self);\n"
              + "             self._handleBindingEntityChanged = function (e) {\n"
              + "                 if (e.detail.value && e.detail.value.entityType) {\n"
              + "                     if (this._prevCurrBindingEntity && e.detail.value.entityType !== this._prevCurrBindingEntity.entityType) {\n"
              + "                         this.fire('tg-master-type-before-change',{\n"
              + "                             prevType: this._prevCurrBindingEntity.entityType,\n"
              + "                             currType: e.detail.value.entityType\n"
              + "                         });\n"
              + "                     }\n"
              + "                     this._prevCurrBindingEntity = e.detail.value;\n"
              + "                 }\n"
              + "             }.bind(self);\n"
              + "             self._handleBindingEntityRetrievedError = function (e) {\n"
              + "                 this.fire('tg-master-navigation-error');\n"
              + "             }.bind(self);\n"
              + "             self.addEventListener('_curr-binding-entity-changed', self._handleBindingEntityChanged);\n"
              + "             self.$.loader.addEventListener('binding-entity-retrieved-error', self._handleBindingEntityRetrievedError);\n");
      this.renderable = () -> new InnerTextElement(masterTemplate);
    }

    @Override
    protected String getAfterLoadListener() {
        return "this._seqEditAfterLoadListener";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }
}
