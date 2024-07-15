package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle("Domain Property Holder")
@CompanionObject(DomainPropertyHolderCo.class)
public class DomainPropertyHolder extends AbstractUnionEntity {
    @IsProperty
    @MapTo
    @Title(value = "Domain type", desc = "Desc")
    private DomainType domainType;
    
    @IsProperty
    @MapTo
    @Title(value = "Domain Property", desc = "Desc")
    private DomainProperty domainProperty;

    @Observable
    public DomainPropertyHolder setDomainProperty(final DomainProperty domainProperty) {
        this.domainProperty = domainProperty;
        return this;
    }

    public DomainProperty getDomainProperty() {
        return domainProperty;
    }

    @Observable
    public DomainPropertyHolder setDomainType(final DomainType domainType) {
        this.domainType = domainType;
        return this;
    }

    public DomainType getDomainType() {
        return domainType;
    }
}