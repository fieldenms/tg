/**
 *
 */
package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(DynamicEntityKey.class)
public class CorrectEntityWithDynamicEntityKey extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    protected Integer property1;

    @IsProperty
    @CompositeKeyMember(2)
    protected Integer property2;

    public Integer getProperty1() {
        return property1;
    }

    @Observable
    public void setProperty1(final Integer property1) {
        this.property1 = property1;
    }

    public Integer getProperty2() {
        return property2;
    }

    @Observable
    public void setProperty2(final Integer property2) {
        this.property2 = property2;
    }
}