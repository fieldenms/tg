package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Type full name")
@CompanionObject(IDomainProperty.class)
@MapEntityTo
public class DomainProperty extends AbstractEntity<DynamicEntityKey> {
    @IsProperty
    @MapTo
    @Title(value = "name", desc = "Desc")
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @Title(value = "holder", desc = "Desc")
    @CompositeKeyMember(2)
    private DomainPropertyHolder holder;

    @IsProperty
    @MapTo
    @Title(value = "domain type", desc = "Desc")
    private DomainType domainType;

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private String title;

    @IsProperty
    @MapTo
    @Title(value = "Key Index", desc = "Desc")
    private Integer keyIndex;

    @IsProperty
    @MapTo
    @Title(value = "Required?", desc = "Desc")
    private boolean required;

    @IsProperty
    @MapTo
    @Title(value = "Db column", desc = "Desc")
    private String dbColumn;

    @Observable
    public DomainProperty setDbColumn(final String dbColumn) {
        this.dbColumn = dbColumn;
        return this;
    }

    public String getDbColumn() {
        return dbColumn;
    }

    @Observable
    public DomainProperty setRequired(final boolean required) {
        this.required = required;
        return this;
    }

    public boolean getRequired() {
        return required;
    }

    @Observable
    public DomainProperty setKeyIndex(final Integer keyIndex) {
        this.keyIndex = keyIndex;
        return this;
    }

    public Integer getKeyIndex() {
        return keyIndex;
    }

    @Observable
    public DomainProperty setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    public DomainProperty setDomainType(final DomainType domainType) {
        this.domainType = domainType;
        return this;
    }

    public DomainType getDomainType() {
        return domainType;
    }

    @Observable
    public DomainProperty setHolder(final DomainPropertyHolder holder) {
        this.holder = holder;
        return this;
    }

    public DomainPropertyHolder getHolder() {
        return holder;
    }

    @Observable
    public DomainProperty setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}