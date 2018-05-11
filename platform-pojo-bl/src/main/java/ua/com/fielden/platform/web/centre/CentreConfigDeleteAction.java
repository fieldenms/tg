package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Functional entity for deleting centre configuration.
 * <p>
 * Key of this functional entity represents the name of configuration to be deleted or empty string for the case of unnamed configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigDeleteAction.class)
@KeyType(String.class)
public class CentreConfigDeleteAction extends AbstractFunctionalEntityWithCentreContext<String> {
    
}