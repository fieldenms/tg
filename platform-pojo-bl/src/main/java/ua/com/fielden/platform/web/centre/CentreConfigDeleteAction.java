package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * Functional entity for deleting centre configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigDeleteAction.class)
@KeyType(NoKey.class)
public class CentreConfigDeleteAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public CentreConfigDeleteAction() {
        setKey(NO_KEY);
    }
    
}