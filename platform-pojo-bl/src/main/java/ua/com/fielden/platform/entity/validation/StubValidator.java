package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * Stub validator, which should be used strictly for association of validation results in cases where there is no real validator. For example, property requiredness does not use
 * any validator and validation login in setter does not use any validator.
 *
 * @author TG Team
 *
 */
public class StubValidator<T> implements IBeforeChangeEventHandler<T> {

    private static final StubValidator singleton = new StubValidator<>();
    
    private StubValidator() {
    }

    public static <T> StubValidator<T> singleton() {
        return singleton;
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        throw new IllegalStateException("Handling BCE with stub validator is illegal. It should only be used for referencing purposes.");
    }

}
