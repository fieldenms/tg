package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtSector extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgtDivision division;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public TgtSector setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgtSector setDivision(final TgtDivision division) {
        this.division = division;
        return this;
    }

    public TgtDivision getDivision() {
        return division;
    }
}