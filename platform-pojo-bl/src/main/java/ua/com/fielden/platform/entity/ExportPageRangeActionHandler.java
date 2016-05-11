package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class ExportPageRangeActionHandler implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean entityPropertyValue) {
        final EntityExportAction entity = (EntityExportAction) property.getEntity();
        if (entityPropertyValue) {
            entity.setAll(false);
            entity.setSelected(false);
            final MetaProperty<Integer> fromPageProperty = entity.getProperty("fromPage");
            final MetaProperty<Integer> toPageProperty = entity.getProperty("toPage");
            fromPageProperty.setEditable(true);
            fromPageProperty.setRequired(true);
            toPageProperty.setEditable(true);
            toPageProperty.setRequired(true);
        }
    }
}
