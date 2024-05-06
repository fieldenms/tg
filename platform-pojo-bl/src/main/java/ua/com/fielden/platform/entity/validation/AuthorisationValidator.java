package ua.com.fielden.platform.entity.validation;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

/**
 * A pre-condition to authorise an attempt to modify a property.
 * By default, authorisation is performed only for persisted entity instances, where the attempted value is different to the original.
 * <p>
 * There are 2 configuration parameters:
 * <ul>
 *     <li>{@code persistedOnly} - {@code true} by default, indicating that authorisation of property modifications is applicable to persisted properties only; {@code false} would be most suitable for action entities.</li>
 *     <li>{@code securityToken} - a security token class checked for access.</li>
 * </ul>
 *
 * @author TG Team
 *
 */
public class AuthorisationValidator extends AbstractBeforeChangeEventHandler<Object> {
    public static final String ERR_MISSING_AUTH_TOKEN = "Restricted access. No security token was configured for authorisation.";

    protected boolean persistedOnly = true;
    protected Class<? extends ISecurityToken> securityToken;
    private final IAuthorisationModel authModel;

    @Inject
    protected AuthorisationValidator(final IAuthorisationModel authModel) {
        this.authModel = authModel;
    }

    @Override
    public Result handle(final MetaProperty<Object> mp, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (securityToken == null) {
            return failure(ERR_MISSING_AUTH_TOKEN);
        }

        // if property validation happens in a context of another authorisation scope, then we simply skip authorisation and return success.
        if (authModel.isStarted()) {
            return successful();
        }

        final AbstractEntity<?> entity = mp.getEntity();

        // authorisation is required only if the value is different to the original and the entity if either persisted or the persistedOnly requirement is false
        if ((entity.isPersisted() || !persistedOnly) && !EntityUtils.equalsEx(newValue, mp.getOriginalValue())) {
            return authModel.authorise(securityToken);
        }
        // otherwise, return success
        return successful();
    }

}
