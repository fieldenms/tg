package ua.com.fielden.platform.web_api.validators;

import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.IEntityTypeEnhancement;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/// Validates [UserDefinedEnhancedEntityType#getName()], which must not be the simple name of a domain entity type.
///
public class EnhancedEntityTypeNameValidator implements IBeforeChangeEventHandler<String> {

    public static final String ERR_NAME_OF_DOMAIN_TYPE = "Name [%s] cannot be used, as domain entity type [%s] has it.";

    private final IApplicationDomainProvider appDomainProvider;
    private final IEntityTypeEnhancement entityTypeEnhancement;

    @Inject
    protected EnhancedEntityTypeNameValidator(
            final IApplicationDomainProvider appDomainProvider,
            final IEntityTypeEnhancement entityTypeEnhancement)
    {
        this.appDomainProvider = appDomainProvider;
        this.entityTypeEnhancement = entityTypeEnhancement;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        final var nameValidation = entityTypeEnhancement.validateName(newValue);
        if (!nameValidation.isSuccessful()) {
            return nameValidation;
        }

        final var domainType = appDomainProvider.entityTypes().stream()
                .filter(type -> type.getSimpleName().equals(newValue))
                .findFirst();
        if (domainType.isPresent()) {
            return failuref(ERR_NAME_OF_DOMAIN_TYPE, newValue, domainType.get().getName());
        }

        return successful();
    }

}
