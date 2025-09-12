package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

@EntityTitle("Domain Property Holder")
@CompanionObject(DomainPropertyHolderCo.class)
public class DomainPropertyHolder extends AbstractUnionEntity {
    @IsProperty
    @MapTo
    @Title(value = "Domain Type", desc = "Domain Type that owns this property")
    private DomainType domainType;
    
    @IsProperty
    @MapTo
    @Title(value = "Domain Property", desc = "Another Domain Property that this property is a sub-property of")
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
