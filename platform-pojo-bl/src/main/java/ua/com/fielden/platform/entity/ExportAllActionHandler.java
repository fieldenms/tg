package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class ExportAllActionHandler implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean entityPropertyValue) {
        final EntityExportAction entity = (EntityExportAction) property.getEntity();
        if (entityPropertyValue) {
            entity.setPageRange(false);
            entity.setSelected(false);
            final MetaProperty<Integer> fromPageProperty = entity.getProperty("fromPage");
            final MetaProperty<Integer> toPageProperty = entity.getProperty("toPage");
            fromPageProperty.setEditable(false);
            fromPageProperty.setRequired(false);
            toPageProperty.setEditable(false);
            toPageProperty.setRequired(false);
        }
    }

}
