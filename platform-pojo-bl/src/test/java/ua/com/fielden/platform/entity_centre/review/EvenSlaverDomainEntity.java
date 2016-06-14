package ua.com.fielden.platform.entity_centre.review;

import java.util.Date;

import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "domain tree representation" testing.
 * 
 * @author TG Team
 * 
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(DynamicEntityKey.class)
public class EvenSlaverDomainEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected EvenSlaverDomainEntity() {
    }

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(1)
    private Integer integerProp = null;
    @IsProperty
    @CompositeKeyMember(2)
    private Double doubleProp = 0.0;
    @IsProperty
    private Date dateProp;

    ////////// A property of entity type //////////
    @IsProperty
    private EntityWithStringKeyType simpleEntityProp;

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public Double getDoubleProp() {
        return doubleProp;
    }

    @Observable
    public void setDoubleProp(final Double doubleProp) {
        this.doubleProp = doubleProp;
    }

    public Date getDateProp() {
        return dateProp;
    }

    @Observable
    public void setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
    }

    public EntityWithStringKeyType getSimpleEntityProp() {
        return simpleEntityProp;
    }

    @Observable
    public void setSimpleEntityProp(final EntityWithStringKeyType simpleEntityProp) {
        this.simpleEntityProp = simpleEntityProp;
    }
}
