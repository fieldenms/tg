package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * Definer for {@link TgPersistentEntityWithProperties#getCosWithACE1()} property (and 2, 1WithDefaultValue, 2WithDefaultValue).
 * 
 * @author TG Team
 *
 */
public class CosWithACEDefiner implements IAfterChangeEventHandler<TgPersistentEntityWithProperties> {
    
    @Override
    public void handle(final MetaProperty<TgPersistentEntityWithProperties> property, final TgPersistentEntityWithProperties entityPropertyValue) {
        final String child1 = property.getName() + "Child1";
        final String child2 = property.getName() + "Child2";
        final TgPersistentEntityWithProperties entity = (TgPersistentEntityWithProperties) property.getEntity();
        if (!entity.isInitialising()) {
            final boolean isKey7 = entityPropertyValue != null && entityPropertyValue.getKey().equals("KEY7");
            entity.set(child1, isKey7 ? entityPropertyValue : null);
            entity.set(child2, isKey7 ? entityPropertyValue : null);
        }
        final boolean isKey8 = entityPropertyValue != null && entityPropertyValue.getKey().equals("KEY8");
        entity.getProperty(child1).setRequired(isKey8);
        entity.getProperty(child2).setEditable(!isKey8);
    }
    
}