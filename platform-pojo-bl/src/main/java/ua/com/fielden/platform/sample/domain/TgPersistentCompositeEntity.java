package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * One-2-Many entity object.
 *
 * @author Developers
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgPersistentCompositeEntity.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
@DisplayDescription
public class TgPersistentCompositeEntity extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @Title(value = "Composite Part 1", desc = "Composite Part 1")
    @MapTo
    @CompositeKeyMember(1)
    private TgPersistentEntityWithProperties key1;

    @IsProperty
    @MapTo
    @Title(value = "Composite Part 2", desc = "Composite Part 2")
    @CompositeKeyMember(2)
    @Optional
    private Integer key2;

    @Observable
    public TgPersistentCompositeEntity setKey2(final Integer key2) {
        this.key2 = key2;
        return this;
    }

    public Integer getKey2() {
        return key2;
    }

    @Observable
    public TgPersistentCompositeEntity setKey1(final TgPersistentEntityWithProperties value) {
        this.key1 = value;
        return this;
    }

    public TgPersistentEntityWithProperties getKey1() {
        return key1;
    }

}