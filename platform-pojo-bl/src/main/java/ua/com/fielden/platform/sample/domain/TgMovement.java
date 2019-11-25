package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = " --> ")
@MapEntityTo
@CompanionObject(ITgMovement.class)
public class TgMovement extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgLocation from;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private TgLocation to;

    @IsProperty
    @MapTo
    private Date date;

    public Date getDate() {
        return date;
    }

    public TgLocation getFrom() {
        return from;
    }

    public TgLocation getTo() {
        return to;
    }

    @Observable
    public TgMovement setFrom(final TgLocation from) {
        this.from = from;
        return this;
    }

    @Observable
    public TgMovement setTo(final TgLocation to) {
        this.to = to;
        return this;
    }
    
    @Observable
    public TgMovement setDate(final Date date) {
        this.date = date;
        return this;
    }
}