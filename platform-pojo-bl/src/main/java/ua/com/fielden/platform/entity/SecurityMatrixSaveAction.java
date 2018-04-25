package ua.com.fielden.platform.entity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents the security matrix save action.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ISecurityMatrixSaveAction.class)
public class SecurityMatrixSaveAction  extends AbstractFunctionalEntityWithCentreContext<NoKey> {

}
