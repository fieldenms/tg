package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
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
@CompanionObject(ITgOrgUnit4.class)
public class TgOrgUnit4 extends AbstractPersistentEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Parent", desc = "Parent")
    @CompositeKeyMember(1)
    private TgOrgUnit3 parent;

    @IsProperty
    @MapTo
    @Title(value = "Name", desc = "Desc")
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public TgOrgUnit4 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgOrgUnit4 setParent(final TgOrgUnit3 parent) {
        this.parent = parent;
        return this;
    }

    public TgOrgUnit3 getParent() {
        return parent;
    }
}