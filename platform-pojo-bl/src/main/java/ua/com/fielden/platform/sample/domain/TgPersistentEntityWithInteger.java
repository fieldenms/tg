package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(ITgPersistentEntityWithInteger.class)
@MapEntityTo
public class TgPersistentEntityWithInteger extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Integer prop;

    @IsProperty
    @MapTo
    @Title(value = "Entity prop", desc = "Entity property")
    private TgPersistentEntityWithInteger entityProp;

    @Observable
    @EntityExists(TgPersistentEntityWithInteger.class)
    public TgPersistentEntityWithInteger setEntityProp(final TgPersistentEntityWithInteger entityProp) {
        this.entityProp = entityProp;
        return this;
    }

    public TgPersistentEntityWithInteger getEntityProp() {
        return entityProp;
    }

    @Observable
    @Max(9999)
    @GreaterOrEqual(-600)
    public TgPersistentEntityWithInteger setProp(final Integer prop) {
        this.prop = prop;
        return this;
    }

    public Integer getProp() {
        return prop;
    }

}