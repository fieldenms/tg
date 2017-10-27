package fielden.test_app.close_leave;

import fielden.test_app.close_leave.TgCloseLeaveExample;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.DescRequired;

/** 
 * One-2-One entity object.
 * 
 * @author Developers
 *
 */
@KeyType(TgCloseLeaveExample.class)
@KeyTitle("Key")
@CompanionObject(ITgCloseLeaveExampleDetail.class)
@MapEntityTo
@DescTitle("Desc")
@DisplayDescription
@DescRequired
public class TgCloseLeaveExampleDetail extends AbstractPersistentEntity<TgCloseLeaveExample> {

}