package ua.com.fielden.platform.web_api.validators;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.IEntityTypeEnhancement;
import ua.com.fielden.platform.web_api.UserDefinedCalculatedProperty;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityTypeCo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;

/// Validates [UserDefinedCalculatedProperty#getName()], which must not be the name of a property of the base type, ignoring case.
///
/// The base type is that of [UserDefinedCalculatedProperty#getEntityType()], which is therefore assigned first.
///
public class UserDefinedCalculatedPropertyNameValidator extends AbstractBeforeChangeEventHandler<String> {

    public static final String
            ERR_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be added, as base type [%s] has it.",
            ERR_INVALID_NAME = "[%s] is not a valid name. A name must be a Java identifier other than a keyword, consisting of letters, digits and underscores, and must not start with a digit or with two underscores.";

    private final IEntityTypeEnhancement entityTypeEnhancement;

    @Inject
    protected UserDefinedCalculatedPropertyNameValidator(final IEntityTypeEnhancement entityTypeEnhancement) {
        this.entityTypeEnhancement = entityTypeEnhancement;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final @Nullable String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        final var nameValidation = entityTypeEnhancement.validateName(newValue);
        if (!nameValidation.isSuccessful()) {
            return nameValidation;
        }

        final UserDefinedCalculatedProperty calculatedProperty = property.getEntity();
        final var entityType = calculatedProperty.getEntityType();
        if (entityType == null) {
            return successful();
        }
        final UserDefinedEnhancedEntityTypeCo coUserDefinedEnhancedEntityType = co(UserDefinedEnhancedEntityType.class);
        final var baseType = coUserDefinedEnhancedEntityType.findBaseTypeAsJavaType(entityType);
        final var baseProperty = baseType.flatMap(type -> streamProperties(type).map(Field::getName).filter(newValue::equalsIgnoreCase).findFirst());
        if (baseProperty.isPresent()) {
            return failuref(ERR_PROPERTY_OF_BASE_TYPE, baseProperty.get(), baseType.get().getSimpleName());
        }
        else {
            return successful();
        }
    }

}
