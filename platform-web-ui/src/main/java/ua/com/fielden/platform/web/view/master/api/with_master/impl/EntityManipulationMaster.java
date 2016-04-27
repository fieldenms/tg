package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractEntityManipulationAction;

class EntityManipulationMaster<T extends AbstractEntityManipulationAction> extends AbstractMasterWithMaster<T> {

    EntityManipulationMaster(final Class<T> entityType, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, null, shouldRefreshParentCentreAfterSave);
    }

    @Override
    protected String getAttributes(final Class<? extends AbstractEntity<?>> entityType, final String bindingEntityName, final boolean shouldRefreshParentCentreAfterSave) {
        return "{" +
                "   currentState: 'EDIT', " +
                "   centreUuid: this.centreUuid, " +
                "   entityId: " + bindingEntityName + ".entityId, " +
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

}
