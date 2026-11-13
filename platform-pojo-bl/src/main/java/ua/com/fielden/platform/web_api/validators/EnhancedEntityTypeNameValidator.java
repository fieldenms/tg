package ua.com.fielden.platform.web_api.validators;

import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;

/// Validates [UserDefinedEnhancedEntityType#getName()], which must not be the simple name of a domain entity type.
///
public class EnhancedEntityTypeNameValidator implements IBeforeChangeEventHandler<String> {

    public static final String ERR_NAME_OF_DOMAIN_TYPE = "Name [%s] cannot be used, as domain entity type [%s] has it.";

    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected EnhancedEntityTypeNameValidator(final IApplicationDomainProvider appDomainProvider) {
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        return validate(newValue, appDomainProvider);
    }

    public static Result validate(final String name, final IApplicationDomainProvider appDomainProvider) {
        return appDomainProvider.entityTypes().stream()
                .filter(type -> type.getSimpleName().equals(name))
                .findFirst()
                .map(type -> failuref(ERR_NAME_OF_DOMAIN_TYPE, name, type.getName()))
                .orElseGet(Result::successful);
    }

}
