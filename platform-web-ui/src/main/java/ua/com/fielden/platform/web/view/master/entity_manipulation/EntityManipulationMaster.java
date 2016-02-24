package ua.com.fielden.platform.web.view.master.entity_manipulation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractEntityManipulationAction;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.AbstractMasterWithMaster;

public class EntityManipulationMaster<T extends AbstractEntityManipulationAction> extends AbstractMasterWithMaster<T> {

    public EntityManipulationMaster(final Class<T> entityType) {
        super(entityType, null);
    }

    @Override
    protected String getAttributes(final Class<? extends AbstractEntity<?>> entityType, final String bindingEntityName) {
        return "{\n" +
                "   currentState: 'EDIT',\n" +
                "   centreUuid: this.centreUuid,\n" +
                "   entityId: " + bindingEntityName + ".entityId,\n" +
                "   entityType: " + bindingEntityName + ".entityType\n" +
                "};\n";
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
