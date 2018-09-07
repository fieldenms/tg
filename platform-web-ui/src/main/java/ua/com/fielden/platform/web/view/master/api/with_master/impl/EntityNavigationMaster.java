package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class EntityNavigationMaster extends EntityManipulationMaster<EntityNavigationAction> {

    private final IRenderable renderable;

    public EntityNavigationMaster(final Class<EntityNavigationAction> entityType, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, shouldRefreshParentCentreAfterSave);
        final String masterTemplate = super.render().render().toString().replace("//@master-is-ready-custom-code",
                "             //Provide custom after load listener\n" +
                "             self._seqEditAfterLoadListener = function (e) {\n" +
                "                 this._assignPostSavedHandlersForEmbeddedMaster(e);\n" +
                "                 const saveButton = e.detail.querySelector(\"tg-action[role='save']\");\n" +
                "                 saveButton.closeAfterExecution = false;\n" +
                "             }.bind(self);\n");
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
