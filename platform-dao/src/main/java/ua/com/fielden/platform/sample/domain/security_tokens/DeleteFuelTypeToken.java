package ua.com.fielden.platform.sample.domain.security_tokens;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Represents a top level security token used for testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Delete Fuel Type", desc = "Controls deletion of fuel types.")
public class DeleteFuelTypeToken implements ISecurityToken {
}
