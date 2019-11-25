package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgLocation.class)
public class TgLocation extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Optional
    private TgLocation parent;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    @IsProperty
    @MapTo
    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public TgLocation getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgLocation setParent(final TgLocation parent) {
        this.parent = parent;
        return this;
    }

    @Observable
    public TgLocation setName(final String name) {
        this.name = name;
        return this;
    }
    
    @Observable
    public TgLocation setLevel(final Integer level) {
        this.level = level;
        return this;
    }
}