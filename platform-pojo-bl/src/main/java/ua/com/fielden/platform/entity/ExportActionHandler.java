package ua.com.fielden.platform.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class ExportActionHandler implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean entityPropertyValue) {
        if (entityPropertyValue) {
            final Set<String> props = new HashSet<String>(Arrays.asList("all", "pageRange", "selected"));
            final EntityExportAction entity = (EntityExportAction) property.getEntity();
            props.remove(property.getName());
            for (final String prop : props) {
                entity.set(prop, false);
            }
            final MetaProperty<Integer> fromPageProperty = entity.getProperty("fromPage");
            final MetaProperty<Integer> toPageProperty = entity.getProperty("toPage");
            final boolean stateToAssign = "pageRange".equals(property.getName());
            fromPageProperty.setEditable(stateToAssign);
            fromPageProperty.setRequired(stateToAssign);
            toPageProperty.setEditable(stateToAssign);
            toPageProperty.setRequired(stateToAssign);
        }
    }

}
