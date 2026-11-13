package ua.com.fielden.platform.web_api.validators;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.UserDefinedCalculatedProperty;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;

/// Validates [UserDefinedCalculatedProperty#getName()], which must not be the name of a property of the base type.
///
/// The base type is that of [UserDefinedCalculatedProperty#getEntityType()], which is therefore assigned first.
///
public class UserDefinedCalculatedPropertyNameValidator implements IBeforeChangeEventHandler<String> {

    public static final String ERR_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be added, as base type [%s] has it.";

    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected UserDefinedCalculatedPropertyNameValidator(final IApplicationDomainProvider appDomainProvider) {
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final @Nullable String newValue, final Set<Annotation> mutatorAnnotations) {
        final UserDefinedCalculatedProperty calculatedProperty = property.getEntity();
        final var entityType = calculatedProperty.getEntityType();
        if (newValue == null || entityType == null) {
            return successful();
        }
        final String name = entityType.getBaseType();
        return appDomainProvider.entityTypes().stream().filter(type -> type.getName().equals(name)).findFirst()
                .map(baseType -> validate(newValue, baseType))
                .orElseGet(Result::successful);
    }

    public static Result validate(final String name, final Class<? extends AbstractEntity<?>> baseType) {
        return isPropertyPresent(baseType, name)
               ? failuref(ERR_PROPERTY_OF_BASE_TYPE, name, baseType.getSimpleName())
               : successful();
    }

}
