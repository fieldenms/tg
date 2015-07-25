package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtZone extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgtSector sector;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public TgtZone setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgtZone setSector(final TgtSector parent) {
        this.sector = parent;
        return this;
    }

    public TgtSector getSector() {
        return sector;
    }
}