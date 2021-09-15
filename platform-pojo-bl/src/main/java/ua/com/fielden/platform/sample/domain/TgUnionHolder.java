package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Type that holds {@link TgUnion} property.
 * 
 * @author TG Team
 *
 */
@EntityTitle("TG Union Holder")
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(TgUnionHolderCo.class)
@MapEntityTo
public class TgUnionHolder extends AbstractPersistentEntity<String> {

    @IsProperty
    @MapTo
    @Title("Union")
    private TgUnion union;

    @Observable
    public TgUnionHolder setUnion(final TgUnion union) {
        this.union = union;
        return this;
    }

    public TgUnion getUnion() {
        return union;
    }

}