package ua.com.fielden.platform.mcp.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/// Validates property [McpAccessToken#getAuthorisationCode()]: an access token can be issued only if the user who granted the authorisation is active.
///
public class McpAccessTokenActiveUserValidator implements IBeforeChangeEventHandler<McpAuthorisationCode> {

    public static final String ERR_USER_INACTIVE = "An access token cannot be issued, as user [%s], who granted the authorisation, is inactive.";

    @Override
    public Result handle(final MetaProperty<McpAuthorisationCode> property, final McpAuthorisationCode newValue, final Set<Annotation> mutatorAnnotations) {
        return newValue != null && !newValue.getUser().isActive()
                ? failuref(ERR_USER_INACTIVE, newValue.getUser())
                : successful();
    }

}
