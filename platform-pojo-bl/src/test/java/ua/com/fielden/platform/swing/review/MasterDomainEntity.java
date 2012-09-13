package ua.com.fielden.platform.swing.review;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Needed for testing chart analysis query generation.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class MasterDomainEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected MasterDomainEntity() {
    }

    ////////// Range types //////////
    @IsProperty
    private Integer integerProp = null;

    @IsProperty
    private Date dateProp;

    ////////// Entity type //////////
    @IsProperty(linkProperty = "masterEntityProp")
    private SlaveDomainEntity entityProp;

    public Integer getIntegerProp() {
	return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
	this.integerProp = integerProp;
    }

    public Date getDateProp() {
	return dateProp;
    }
    @Observable
    public void setDateProp(final Date dateProp) {
	this.dateProp = dateProp;
    }

    public SlaveDomainEntity getEntityProp() {
	return entityProp;
    }
    @Observable
    public void setEntityProp(final SlaveDomainEntity entityProp) {
	this.entityProp = entityProp;
    }
}
