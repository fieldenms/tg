package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class EntityNavigationMaster extends EntityManipulationMaster<EntityNavigationAction> {

    private final IRenderable renderable;

    public EntityNavigationMaster(final Class<EntityNavigationAction> entityType, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, shouldRefreshParentCentreAfterSave);
        final String masterTemplate = super.render().render().toString().replace("//@master-is-ready-custom-code",
                "             //Provide custom after load listener\n"
              + "             self._seqEditAfterLoadListener = function (e) {\n"
              + "                 this._assignPostSavedHandlersForEmbeddedMaster(e);\n"
              + "                 const saveButton = e.detail.querySelector(\"tg-action[role='save']\");\n"
              + "                 saveButton.closeAfterExecution = false;\n"
              + "             }.bind(self);\n"
              + "             self._handleBindingEntityAppeared = function (e) {\n"
              + "                 if (this._previousMaster && this._previousMaster.entityType !== this.$.loader.loadedElement.entityType) {\n"
              + "                     this.fire('tg-master-type-changed',{\n"
              + "                         prevType: this._previousMaster.entityType,\n"
              + "                         currType: this.$.loader.loadedElement.entityType\n"
              + "                     });\n"
              + "                 }\n"
              + "                 this._previousMaster = this.$.loader.loadedElement;\n"
              + "             }.bind(self);\n"
              + "             self._handleBindingEntityChanged = function (e) {\n"
              + "                 if (e.detail.value) {\n"
              + "                     if (this._prevCurrBindingEntity && e.detail.value.entityType !== this._prevCurrBindingEntity.entityType) {\n"
              + "                         this.fire('tg-master-type-before-change',{\n"
              + "                             prevType: this._prevCurrBindingEntity.entityType,\n"
              + "                             currType: e.detail.value.entityType\n"
              + "                         });\n"
              + "                     }\n"
              + "                     this._prevCurrBindingEntity = e.detail.value;\n"
              + "                 }\n"
              + "             }.bind(self);\n"
              + "             self.addEventListener('binding-entity-appeared', self._handleBindingEntityAppeared);\n"
              + "             self.addEventListener('_curr-binding-entity-changed', self._handleBindingEntityChanged);\n");
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
