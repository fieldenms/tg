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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgOrgUnit3.class)
public class TgOrgUnit3 extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Parent", desc = "Parent")
    @CompositeKeyMember(1)
    private TgOrgUnit2 parent;

    @IsProperty
    @MapTo
    @Title(value = "Name", desc = "Desc")
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public TgOrgUnit3 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgOrgUnit3 setParent(final TgOrgUnit2 parent) {
        this.parent = parent;
        return this;
    }

    public TgOrgUnit2 getParent() {
        return parent;
    }
}