package ua.com.fielden.platform.web_api.validators;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/// Validates [UserDefinedEnhancedEntityType#getBaseType()], which must be the fully-qualified name of a domain entity type.
///
public class EnhancedEntityTypeBaseTypeValidator implements IBeforeChangeEventHandler<String> {

    public static final String ERR_NO_SUCH_DOMAIN_TYPE = "Domain entity type [%s] does not exist.";

    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected EnhancedEntityTypeBaseTypeValidator(final IApplicationDomainProvider appDomainProvider) {
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final @Nullable String newValue, final Set<Annotation> mutatorAnnotations) {
        return newValue == null || appDomainProvider.entityTypes().stream().anyMatch(type -> type.getName().equals(newValue))
               ? successful()
               : failuref(ERR_NO_SUCH_DOMAIN_TYPE, newValue);
    }

}
