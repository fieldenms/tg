package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
/**
 * Entity with {@link DynamicEntityKey} without transactional composite key members.
 *
 * @author TG Team
 */
@SuppressWarnings("serial")
@KeyType(DynamicEntityKey.class)
public class CompositionalEntity extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    private Integer key1;

    @IsProperty
    @CompositeKeyMember(2)
    private String key2;

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

}