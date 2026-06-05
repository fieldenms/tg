package fielden.test_app.close_leave;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

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

    @Override
    @Observable
    public TgCloseLeaveExampleDetail setKey(TgCloseLeaveExample key) {
        super.setKey(key);
        return this;
    }

    @Override
    @Observable
    public TgCloseLeaveExampleDetail setDesc(String desc) {
        return super.setDesc(desc);
    }

}
