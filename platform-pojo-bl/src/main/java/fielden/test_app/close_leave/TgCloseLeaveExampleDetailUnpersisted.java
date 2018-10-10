package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(TgCloseLeaveExample.class)
@KeyTitle("Key")
@CompanionObject(ITgCloseLeaveExampleDetailUnpersisted.class)
@MapEntityTo
@DescTitle("Desc")
@DisplayDescription
@DescRequired
public class TgCloseLeaveExampleDetailUnpersisted extends AbstractPersistentEntity<TgCloseLeaveExample> {

}