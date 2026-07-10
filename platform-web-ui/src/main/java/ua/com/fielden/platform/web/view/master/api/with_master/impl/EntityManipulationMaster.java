package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractEntityManipulationAction;

public class EntityManipulationMaster<T extends AbstractEntityManipulationAction> extends AbstractMasterWithMaster<T> {

    protected EntityManipulationMaster(final Class<T> entityType, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, null, shouldRefreshParentCentreAfterSave);
    }

    /**
     * Indicates whether the SAVE action of the embedded master may close the enclosing dialog.
     * Masters that govern closing themselves should override this method to return `false`.
     *
     * The returned value is passed to the embedded master as property `shouldCloseAfterSave` (see `tg-entity-master-behavior`).
     * `tg-element-loader` assigns that property before inserting the embedded master into the DOM, that is, before the embedded master gets connected.
     * Therefore, the value is guaranteed to be in place by the time the embedded master's `ready` callback runs, which is where it takes effect.
     *
     * This method is invoked from a superclass constructor by way of `getAttributes`.
     * Overriding implementations must return a constant, and must not depend on subclass state.
     */
    protected boolean shouldCloseAfterSave() {
        return true;
    }

    @Override
    protected String getAttributes(final Class<? extends AbstractEntity<?>> entityType, final String bindingEntityName, final boolean shouldRefreshParentCentreAfterSave) {
        return "{" +
                "   currentState: 'EDIT', " +
                "   centreUuid: this.centreUuid, " +
                "   excludeInsertionPoints: this.excludeInsertionPoints, " +
                "   entityId: " + bindingEntityName + ".entityId, " +
                "   entityType: " + bindingEntityName + ".entityType, " +
                "   shouldCloseAfterSave: " + shouldCloseAfterSave() + ", " +
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
