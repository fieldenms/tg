package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

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

    @Override
    @Observable
    public TgCloseLeaveExample setDesc(String desc) {
        return super.setDesc(desc);
    }

}
