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
 *     <li>{@code securityToken} - a security token class checked for access.</li>
 *     <li>{@code persistedOnly} - {@code true} by default, indicating that authorisation of property modifications is applicable to persisted entities only;
 *                                 {@code false} would be most suitable for action entities or where situations where default values are permissible, but their change requires special permissions.</li>
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
        // If securityToken is null then should restrict access and report misconfiguration (this would be an implementation error).
        if (securityToken == null) {
            return failure(ERR_MISSING_AUTH_TOKEN);
        }

        // If property validation happens in a context of another authorisation scope, then we simply skip authorisation and return success.
        if (authModel.isStarted()) {
            return successful();
        }
        // Property authorisation should be performed only if it is requested outside another authorisation scope.
        // There is no need to manage authorisation scope by indicating its start in this case (i.e., no need to authModel.start()), as there can be no subsequent nested authorisation scopes.
        // Property authorisation starts and finishes within the scope of this method.
        else {
            final AbstractEntity<?> entity = mp.getEntity();
            // Authorisation is required only if the entity is either persisted or the persistedOnly requirement is false, and the newValue is different to the original property value.
            if ((entity.isPersisted() || !persistedOnly) && !EntityUtils.equalsEx(newValue, mp.getOriginalValue())) {
                return authModel.authorise(securityToken);
            }
            // Otherwise, return success.
            return successful();
        }
    }

}
