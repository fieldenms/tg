package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.Final;

/**
 * Entity with {@link DynamicEntityKey} for testing purposes
 *
 * @author TG Team
 */
@KeyType(DynamicEntityKey.class)
public class EntityWithDynamicEntityKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @Final(persistedOnly = false)
    private Integer key1;

    @IsProperty
    @CompositeKeyMember(2)
    @Final(persistedOnly = false)
    private String key2;

    @IsProperty
    @CompositeKeyMember(3)
    @Final(persistedOnly = false)
    private Entity key3;

    public Integer getKey1() {
        return key1;
    }

    @Observable
    public void setKey1(final Integer key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    @Observable
    public void setKey2(final String key2) {
        this.key2 = key2;
    }

    public Entity getKey3() {
        return key3;
    }

    @Observable
    public void setKey3(final Entity key3) {
        this.key3 = key3;
    }

}