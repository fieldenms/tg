package ua.com.fielden.platform.web_api.validators;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.web_api.GraphQLCommon;
import ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.web_api.FieldVisibility.isModelReadable;

/// Validates [UserDefinedEnhancedEntityType#getBaseType()], which must be the fully-qualified name of a domain entity type.
///
public class EnhancedEntityTypeBaseTypeValidator implements IBeforeChangeEventHandler<String> {

    public static final String
            ERR_NO_SUCH_BASE_TYPE = "[%s] cannot be a base type. A base type must be a queryable domain entity type.";

    private final IApplicationDomainProvider appDomainProvider;
    private final GraphQLCommon graphQLCommon;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    protected EnhancedEntityTypeBaseTypeValidator(
            final IApplicationDomainProvider appDomainProvider,
            final GraphQLCommon graphQLCommon,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.graphQLCommon = graphQLCommon;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final @Nullable String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        final var javaType = appDomainProvider.entityTypes().stream().filter(type -> type.getCanonicalName().equals(newValue)).findFirst();
        if (javaType.isEmpty()) {
            return failuref(ERR_NO_SUCH_BASE_TYPE, newValue);
        }

        if (!graphQLCommon.isQueryableType(javaType.get())) {
            return failuref(ERR_NO_SUCH_BASE_TYPE, newValue);
        }

        if (!isModelReadable(javaType.get(), authorisationModel, securityTokenProvider)) {
            return failuref(ERR_NO_SUCH_BASE_TYPE, newValue);
        }

        return successful();
    }

}
