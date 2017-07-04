package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * An entity for loading default centre configuration into {@link CentreConfigUpdater} master.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(ICentreConfigUpdaterDefaultAction.class)
public class CentreConfigUpdaterDefaultAction extends AbstractFunctionalEntityWithCentreContext<String> {
    
}
