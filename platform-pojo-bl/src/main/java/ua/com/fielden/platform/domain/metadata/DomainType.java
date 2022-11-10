package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle("Domain Type")
@DescTitle("Description")
@DescRequired
@CompanionObject(DomainTypeCo.class)
@MapEntityTo
@DenyIntrospection
public class DomainType extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title("Table name")
    private String dbTable;

    @IsProperty
    @MapTo
    @Title("Entity Type Description")
    private String entityTypeDesc;

    @IsProperty
    @MapTo
    @Title(value = "Is Entity?", desc = "True for persistent and synthetic entities; false for union entities and other types.")
    private boolean entity;

    @IsProperty
    @MapTo
    @Title(value = "Props count", desc = "The number of properties in this type.")
    private int propsCount;

    @Observable
    public DomainType setPropsCount(final int propsCount) {
        this.propsCount = propsCount;
        return this;
    }

    public int getPropsCount() {
        return propsCount;
    }

    @Observable
    public DomainType setEntity(final boolean entity) {
        this.entity = entity;
        return this;
    }

    public boolean getEntity() {
        return entity;
    }

    @Observable
    public DomainType setEntityTypeDesc(final String entityTypeDesc) {
        this.entityTypeDesc = entityTypeDesc;
        return this;
    }

    public String getEntityTypeDesc() {
        return entityTypeDesc;
    }

    @Observable
    public DomainType setDbTable(final String dbTable) {
        this.dbTable = dbTable;
        return this;
    }

    public String getDbTable() {
        return dbTable;
    }
}