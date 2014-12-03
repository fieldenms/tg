package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity Type Name", desc = "Entity Type Name description")
@CompanionObject(IEntityTypeInfo.class)
public class EntityTypeInfo extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Number", desc = "Number of the type in context of other types for serialisation")
    private Long number;

    @Observable
    public EntityTypeInfo setNumber(final Long number) {
        this.number = number;
        return this;
    }

    public Long getNumber() {
        return number;
    }

}