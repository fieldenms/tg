package ua.com.fielden.platform.web.test.server.master_action;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(INewEntityAction.class)
public class NewEntityAction extends AbstractFunctionalEntityWithCentreContext<String> {

    private static final long serialVersionUID = 1L;
    
}