package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Domain Property")
@DescTitle("Description")
@CompanionObject(DomainPropertyCo.class)
@MapEntityTo
@DenyIntrospection
public class DomainProperty extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title("Property Name")
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @Title(value = "Holder", desc = "Indicates a type where this property belongs, which may be an entity type or a component-like type (e.g. a union type).")
    @CompositeKeyMember(2)
    private DomainPropertyHolder holder;

    // Re-introduce `desc` to make it more than 255 characters
    @IsProperty(length = 1024)
    @MapTo
    @Title("Description")
    private String desc;

    @IsProperty
    @MapTo
    @Title(value = "Domain Type", desc = "An type for this property, which could be an entity type, but one of the primitive types such as Long, Date, etc.")
    private DomainType domainType;

    @IsProperty
    @MapTo
    @Title("Title")
    private String title;

    @IsProperty
    @MapTo
    @Title(value = "Key Index", desc = "Indicates is this property belongs to a key. Non-key prop -- null, Non-composite key prop -- 0, Composite key member prop -- 1 .. n")
    private Integer keyIndex;

    @IsProperty
    @MapTo
    @Title("Required?")
    private boolean required;

    @IsProperty
    @MapTo
    @Title("DB Column")
    private String dbColumn;

    @IsProperty
    @MapTo
    @Required
    @Title("Position")
    private Integer position;

    @Observable
    public DomainProperty setPosition(final Integer position) {
        this.position = position;
        return this;
    }

    public Integer getPosition() {
        return position;
    }

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

    @Override
    @Observable
    public DomainProperty setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}