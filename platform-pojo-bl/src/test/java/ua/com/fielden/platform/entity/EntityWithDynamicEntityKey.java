package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Entity with {@link DynamicEntityKey} for testing purposes
 * 
 * @author Yura, Jhou
 */
@SuppressWarnings("serial")
@KeyType(DynamicEntityKey.class)
public class EntityWithDynamicEntityKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    private Integer key1;

    @IsProperty
    @CompositeKeyMember(2)
    private String key2;

    @IsProperty
    @CompositeKeyMember(3)
    private Entity key3;

    protected EntityWithDynamicEntityKey() {
	super(null, null, "");
	setKey(new DynamicEntityKey(this));
    }

    public Integer getKey1() {
	return key1;
    }

    @NotNull
    @Final
    @Observable
    public void setKey1(final Integer key1) {
	this.key1 = key1;
    }

    public String getKey2() {
	return key2;
    }

    @NotNull
    @Final
    @Observable
    public void setKey2(final String key2) {
	this.key2 = key2;
    }

    public Entity getKey3() {
	return key3;
    }

    @NotNull
    @Final
    @Observable
    public void setKey3(final Entity key3) {
	this.key3 = key3;
    }

}