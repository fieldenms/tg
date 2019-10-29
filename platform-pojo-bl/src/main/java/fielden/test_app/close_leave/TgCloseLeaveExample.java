package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.DescRequired;

/** 
 * Entity to represent Close / Leave behaviour example (and unit test basis) for standalone / compound entity masters.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCloseLeaveExample.class)
@MapEntityTo
@DescTitle("Desc")
@DisplayDescription
@DescRequired
public class TgCloseLeaveExample extends AbstractPersistentEntity<String> {
    
}