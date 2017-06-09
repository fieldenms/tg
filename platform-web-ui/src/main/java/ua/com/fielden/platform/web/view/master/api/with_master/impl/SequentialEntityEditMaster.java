package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.SequentialEntityEditAction;

public class SequentialEntityEditMaster extends AbstractMasterWithMaster<SequentialEntityEditAction> {

    public SequentialEntityEditMaster(final boolean shouldRefreshParentCentreAfterSave) {
        super(SequentialEntityEditAction.class, null, shouldRefreshParentCentreAfterSave);
    }

    @Override
    protected String getAttributes(final Class<? extends AbstractEntity<?>> entityType, final String bindingEntityName, final boolean shouldRefreshParentCentreAfterSave) {
        return "{" +
                "   currentState: 'EDIT', " +
                "   centreUuid: this.centreUuid, " +
                "   entityType: " + bindingEntityName + ".entityType, " +
                "   shouldRefreshParentCentreAfterSave: " + shouldRefreshParentCentreAfterSave +
                "};";
    }

    @Override
    protected String getElementName(final Class<? extends AbstractEntity<?>> entityType) {
        return "'[[_currBindingEntity.elementName]]'";
    }

    @Override
    protected String getImportUri(final Class<? extends AbstractEntity<?>> entityType) {
        return "'[[_currBindingEntity.importUri]]'";
    }

    @Override
    protected String afterLoadCallback(final String embededMaster, final String bindingEntity) {
        return embededMaster + ".postSaved = (function(potentiallySavedOrNewEntity, newBindingEntity){\n" +
                    "this."+ bindingEntity + ".entitiesToEdit.shift();\n" + 
                    "this.postSaved(potentiallySavedOrNewEntity, newBindingEntity);\n" +
                    /*embededMaster + ".entityId = this."+ bindingEntity + ".entitiesToEdit[0];\n" + */
                    embededMaster + ".entityId = 'find_or_new';\n" +
                    embededMaster + ".retrieve(" + embededMaster + ".getMasterEntity());\n"
                + "}).bind(this);\n" + 
                embededMaster + ".postSavedError = this.postSavedError;\n";
    }

}
