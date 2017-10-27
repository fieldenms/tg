package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
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
@CompanionObject(IOpenTgCloseLeaveExampleMasterAction.class)
public class OpenTgCloseLeaveExampleMasterAction extends AbstractFunctionalEntityToOpenCompoundMaster<TgCloseLeaveExample> {

}