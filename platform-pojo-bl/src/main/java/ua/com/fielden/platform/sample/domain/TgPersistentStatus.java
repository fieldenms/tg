package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgPersistentStatus.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgPersistentStatus extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @Override
    @Observable
    public TgPersistentStatus setDesc(String desc) {
        return super.setDesc(desc);
    }

}
