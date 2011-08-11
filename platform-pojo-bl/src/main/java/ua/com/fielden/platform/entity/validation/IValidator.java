package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.ioc.ObservableMutatorInterceptor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * Contract that should be implemented by any validation class.
 * 
 * The idea is that any class implementing this interface should be thread-safe.
 * 
 * @author 01es
 * 
 */
public interface IValidator {
    /**
     * Performs validation (e.g. may perform DB query).
     * <p>
     * Returns extended result of the last validation including custom validation message, exception etc. Method {@link Result#isSuccessful()} should be used for quick evaluation
     * of the validation success.
     * 
     * @param property
     *            -- meta-property for the entity property being validated
     * @param newValue
     *            -- a new value, which is a validation subject and the one that will be set as an entity property value if validation succeeds.
     * @param oldValue
     *            -- an old or other words current property values, which might be needed for validation; in case of {@link ObservableMutatorInterceptor} old value means current
     *            value; in case of re validation old value means previous value.
     * @param mutatorAnnotations
     *            -- a set of annotations defined for a method representing a mutator changing property's value
     * 
     * @return
     */
    Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations); // final Method mutator
}