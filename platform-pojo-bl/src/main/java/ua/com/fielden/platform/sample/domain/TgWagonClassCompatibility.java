package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * Represents the compatibility between certain wagon and bogie classes.
 * 
 * @author nc
 * 
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgWagonClassCompatibility.class)
public class TgWagonClassCompatibility extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgWagonClass wagonClass;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private TgBogieClass bogieClass;

    @IsProperty
    @MapTo
    private String status;

    public TgWagonClassCompatibility() {
        setKey(new DynamicEntityKey(this));
    }

    public TgWagonClass getWagonClass() {
        return wagonClass;
    }

    @Observable
    public TgWagonClassCompatibility setWagonClass(final TgWagonClass wagonClass) {
        this.wagonClass = wagonClass;
        return this;
    }

    public TgBogieClass getBogieClass() {
        return bogieClass;
    }

    @Observable
    public TgWagonClassCompatibility setBogieClass(final TgBogieClass bogieClass) {
        this.bogieClass = bogieClass;
        return this;
    }

    public String getStatus() {
        return status;
    }

    @Observable
    public TgWagonClassCompatibility setStatus(final String status) {
        this.status = status;
        return this;
    }
}