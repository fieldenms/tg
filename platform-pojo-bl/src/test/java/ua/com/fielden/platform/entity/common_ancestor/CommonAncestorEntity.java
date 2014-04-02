/**
 *
 */
package ua.com.fielden.platform.entity.common_ancestor;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(DynamicEntityKey.class)
public class CommonAncestorEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(2)
    protected Long property2;

    public Long getProperty2() {
        return property2;
    }

    @Observable
    public void setProperty2(final Long property2) {
        this.property2 = property2;
    }
}