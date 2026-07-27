package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@EntityTitle(value = "Empty entity", desc = "The entity without any properties for testing")
public class EmptyEntity extends AbstractEntity<String> {

    @Override
    @Observable
    public EmptyEntity setDesc(String desc) {
        return super.setDesc(desc);
    }
}
