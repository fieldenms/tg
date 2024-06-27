package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * This is a preliminary interface for a meta-property factory.
 *
 * @author TG Team
 */
public interface IMetaPropertyFactory {

    /**
     * Takes an annotation instance and instantiates validators based on that annotation.
     * <p>
     * If the passed annotation is not recognised as the validation annotation then an empty array should be returned or an exception thrown.
     */
    IBeforeChangeEventHandler<?>[] create(
            final Annotation annotation,
            final AbstractEntity<?> entity,
            final String propertyName,
            final Class<?> propertyType) throws Exception;

    /**
     * Instantiates property ACE handler based on entity type and property field.
     */
    IAfterChangeEventHandler<?> create(
            final AbstractEntity<?> entity,
            final Field propertyField) throws Exception;

}
