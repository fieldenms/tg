package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(TgCloseLeaveExample.class)
@KeyTitle("Key")
@CompanionObject(ITgCloseLeaveExampleMaster_OpenMain_MenuItem.class)
public class TgCloseLeaveExampleMaster_OpenMain_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<TgCloseLeaveExample>  {

}