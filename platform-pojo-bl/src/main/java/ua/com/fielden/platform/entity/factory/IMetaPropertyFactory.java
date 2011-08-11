package ua.com.fielden.platform.entity.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IMetaPropertyDefiner;
import ua.com.fielden.platform.entity.validation.IValidator;

/**
 * This is a preliminary interface for a meta-property factory.
 * 
 * @author 01es
 * 
 */
public interface IMetaPropertyFactory {
    /**
     * Takes an annotation instance and tries to instantiate a validator based on that annotation.
     * <p>
     * If the passed annotation is not recognised as the validation annotation then null is returned.
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
    IValidator create(//
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
}
