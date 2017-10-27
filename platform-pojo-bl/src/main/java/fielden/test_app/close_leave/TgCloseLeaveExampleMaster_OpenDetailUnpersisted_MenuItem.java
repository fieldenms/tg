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
@CompanionObject(ITgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem.class)
public class TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<TgCloseLeaveExample> {

}