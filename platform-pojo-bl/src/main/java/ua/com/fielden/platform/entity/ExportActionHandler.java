package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.EntityExportAction.EXPORT_OPTION_PROPERTIES;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_EXPORT_TOP;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_NUMBER;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class ExportActionHandler implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean entityPropertyValue) {
        if (entityPropertyValue) {
            final EntityExportAction entity = (EntityExportAction) property.getEntity();
            EXPORT_OPTION_PROPERTIES.stream()
            .filter(propName -> !propName.equals(property.getName()) && entity.<Boolean>get(propName))
            .forEach(propName -> entity.set(propName, false));

            final boolean stateToAssign = PROP_EXPORT_TOP.equals(property.getName());
            final MetaProperty<Integer> numberProperty = entity.getProperty(PROP_NUMBER);
            numberProperty.setEditable(stateToAssign);
            numberProperty.setRequired(stateToAssign);
        }
    }

}
