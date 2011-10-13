/**
 *
 */
package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(DynamicEntityKey.class)
public class CorrectEntityWithDynamicEntityKey extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    protected Long property1;
    @IsProperty
    @CompositeKeyMember(2)
    protected Long property2;

    public Long getProperty1() {
	return property1;
    }

    @Observable
    public void setProperty1(final Long property1) {
	this.property1 = property1;
    }

    public Long getProperty2() {
	return property2;
    }

    @Observable
    public void setProperty2(final Long property2) {
	this.property2 = property2;
    }
}