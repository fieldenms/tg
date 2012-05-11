package ua.com.fielden.platform.associations.one2many.incorrect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Type representing the details side of One-to-Many association.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntity2 extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Integer key2;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Integer key3;

    @IsProperty
    @MapTo
    // @CompositeKeyMember(3) -- intentionally made non-key member
    private MasterEntity2 key1;

    @Observable
    public DetailsEntity2 setKey3(final Integer key3) {
	this.key3 = key3;
	return this;
    }

    public Integer getKey3() {
	return key3;
    }

    @Observable
    public DetailsEntity2 setKey2(final Integer key2) {
	this.key2 = key2;
	return this;
    }

    public Integer getKey2() {
	return key2;
    }

    @Observable
    public DetailsEntity2 setKey1(final MasterEntity2 key1) {
	this.key1 = key1;
	return this;
    }

    public MasterEntity2 getKey1() {
	return key1;
    }
}
