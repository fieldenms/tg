package ua.com.fielden.platform.entity.meta.test_entities.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithCircularDependentPropertiesAndDefiners;

/**
 * Assigns requiredness to both properties {@code two} and {@code three} if both have no values.
 * This definer is used for testing revalidation of dependent properties with circular dependency and requiredness that is managed via definers.
 *
 * @author TG Team
 *
 */
public class EntityWithCircularDependentPropertiesAndDefiners_PropTwoAndThree_Definer implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String entityPropertyValue) {
        final EntityWithCircularDependentPropertiesAndDefiners entity = (EntityWithCircularDependentPropertiesAndDefiners) property.getEntity();
        final boolean required = entity.getTwo() == null && entity.getThree() == null;
        entity.getProperty("two").setRequired(required);
        entity.getProperty("three").setRequired(required);
    }

}
