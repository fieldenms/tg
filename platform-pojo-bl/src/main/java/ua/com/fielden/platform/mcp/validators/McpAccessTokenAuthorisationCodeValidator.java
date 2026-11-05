package ua.com.fielden.platform.mcp.validators;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.utils.IDates;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

/// Validates property [McpAccessToken#getAuthorisationCode()]: an access token can be issued only in exchange for a redeemed authorisation code that has not expired.
///
public class McpAccessTokenAuthorisationCodeValidator implements IBeforeChangeEventHandler<McpAuthorisationCode> {

    public static final String
            ERR_CODE_NOT_REDEEMED = "An access token can be issued only in exchange for a redeemed authorisation code.",
            ERR_CODE_EXPIRED = "An access token cannot be issued in exchange for an expired authorisation code.";

    private final IDates dates;

    @Inject
    McpAccessTokenAuthorisationCodeValidator(final IDates dates) {
        this.dates = dates;
    }

    @Override
    public Result handle(final MetaProperty<McpAuthorisationCode> property, final McpAuthorisationCode newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }
        if (newValue.isActive()) {
            return failure(ERR_CODE_NOT_REDEEMED);
        }
        if (!dates.serverNow().toDate().before(newValue.getExpirationDate())) {
            return failure(ERR_CODE_EXPIRED);
        }
        return successful();
    }

}
