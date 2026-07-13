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

    /**
     * Announces an imminent change of the embedded master's type by firing `tg-master-type-before-change`.
     * The enclosing dialog reacts to this event (see `tg-custom-action-dialog._handleMasterBeforeChange`) to update its title and to start the resize animation before the new master is rendered.
     *
     * This applies to every manipulation master, as `entityType` is a property of {@link AbstractEntityManipulationAction}.
     * The event is fired only when the type actually changes, so masters bound to a single entity type never fire it.
     */
    @Override
    protected String getAdditionalReadyCallbackCode() {
        return "this._handleBindingEntityChanged = function (e) {\n"
             + "    if (e.detail.value && e.detail.value.entityType) {\n"
             + "        if (this._prevCurrBindingEntity && e.detail.value.entityType !== this._prevCurrBindingEntity.entityType) {\n"
             + "            this.fire('tg-master-type-before-change', {\n"
             + "                prevType: this._prevCurrBindingEntity.entityType,\n"
             + "                currType: e.detail.value.entityType\n"
             + "            });\n"
             + "        }\n"
             + "        this._prevCurrBindingEntity = e.detail.value;\n"
             + "    }\n"
             + "}.bind(this);\n"
             + "this.addEventListener('_curr-binding-entity-changed', this._handleBindingEntityChanged);\n";
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
