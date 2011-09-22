package ua.com.fielden.platform.entity.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IMetaPropertyDefiner;
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
     * Takes an annotation instance and tries to instantiate validators based on that annotation.
     * <p>
     * If the passed annotation is not recognised as the validation annotation then an empty array should returned or an exception thrown.
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
     * Instantiates meta-property definer based on entity type and property name.
     *
     * @param entity
     * @param propertyName
     * @return
     * @throws Exception
     */
    IMetaPropertyDefiner create(//
    final AbstractEntity<?> entity,//
    final String propertyName) throws Exception;

    /**
     * Sets injector, which can be used for instantiation of property before and after event handlers.
     *
     * @param injector
     */
    void setInjector(final Injector injector);
}
