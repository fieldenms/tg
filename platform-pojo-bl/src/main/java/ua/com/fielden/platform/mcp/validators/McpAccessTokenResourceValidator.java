package ua.com.fielden.platform.mcp.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAccessToken;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/// Validates property [McpAccessToken#getResource()]: an access token is issued for the resource that its authorisation code was issued for.
///
/// A resource can be assigned before the authorisation code, in which case it is validated once the code is assigned.
///
public class McpAccessTokenResourceValidator implements IBeforeChangeEventHandler<String> {

    public static final String ERR_RESOURCE_MISMATCH = "Resource [%s] differs from resource [%s] of the authorisation code.";

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        final McpAccessToken accessToken = property.getEntity();
        final var authorisationCode = accessToken.getAuthorisationCode();
        return newValue == null || authorisationCode == null || newValue.equals(authorisationCode.getResource())
                ? successful()
                : failuref(ERR_RESOURCE_MISMATCH, newValue, authorisationCode.getResource());
    }

}
