package ua.com.fielden.platform.entity.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

import com.google.inject.Injector;

/**
 * This is a preliminary interface for a meta-property factory.
 *
 * @author TG Team
 *
 */
public interface IMetaPropertyFactory {
    /**
     * Takes an annotation instance and instantiates validators based on that annotation.
     * <p>
     * If the passed annotation is not recognised as the validation annotation then an empty array should be returned or an exception thrown.
     *
     * @param <K>
     * @param annotation
     * @param entity
     * @param keyType
     * @param propertyName
     * @param propertyType
     * @return
     * @throws Exception
     */
    IBeforeChangeEventHandler[] create(//
    final Annotation annotation, //
    final AbstractEntity<?> entity,//
    final String propertyName,//
    final Class<?> propertyType) throws Exception;

    /**
     * Instantiates property ACE handler based on entity type and property field.
     *
     * @param entity
     * @param propertyField
     * @return
     * @throws Exception
     */
    IAfterChangeEventHandler create(//
    final AbstractEntity<?> entity,//
    final Field propertyField) throws Exception;

    /**
     * Sets injector, which can be used for instantiation of property before and after event handlers.
     *
     * @param injector
     */
    void setInjector(final Injector injector);
}
