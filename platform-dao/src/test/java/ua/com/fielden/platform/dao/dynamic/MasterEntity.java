package ua.com.fielden.platform.dao.dynamic;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.interception.AccessToken;
import ua.com.fielden.platform.security.interception.NoAccessToken;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for "dynamic query" testing.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@MapEntityTo("MASTER_ENTITY")
public class MasterEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    ////////// Range types //////////
    @IsProperty
    @MapTo("INTEGER_PROP")
    private Integer integerProp = null;

    @IsProperty
    @MapTo("BIG_DECIMAL_PROP")
    private BigDecimal bigDecimalProp = new BigDecimal(0.0);

    @IsProperty
    @MapTo("MONEY_PROP")
    private Money moneyProp;
    @IsProperty
    @MapTo("DATE_PROP")
    private Date dateProp;

    ////////// boolean type //////////
    @IsProperty
    @MapTo("BOOLEAN_PROP")
    private boolean booleanProp = false;

    ////////// String type //////////
    @IsProperty
    @MapTo("STRING_PROP")
    private String stringProp;

    ////////// Entity type //////////
    @IsProperty
    @MapTo("ENTITY_PROP")
    private SlaveEntity entityProp;

    ///////// Collections /////////
    @IsProperty(SlaveEntity.class)
    private List<SlaveEntity> collection = new ArrayList<SlaveEntity>();

    @IsProperty
    @MapTo("AUTHORISED_PROP")
    @Authorise(AccessToken.class)
    private Money authorisedProp;

    @IsProperty
    @MapTo("UNAUTHORISED_PROP")
    @Authorise(NoAccessToken.class)
    private Money unauthorisedProp;

    public Money getUnauthorisedProp() {
        return unauthorisedProp;
    }

    @Observable
    public MasterEntity setUnauthorisedProp(final Money unauthorisedProp) {
        this.unauthorisedProp = unauthorisedProp;
        return this;
    }

    public Money getAuthorisedProp() {
        return authorisedProp;
    }

    @Observable
    public MasterEntity setAuthorisedProp(final Money authorisedProp) {
        this.authorisedProp = authorisedProp;
        return this;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }

    @Observable
    public void setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public void setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
    }

    public Date getDateProp() {
        return dateProp;
    }

    @Observable
    public void setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
    }

    public boolean isBooleanProp() {
        return booleanProp;
    }

    @Observable
    public void setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
    }

    public String getStringProp() {
        return stringProp;
    }

    @Observable
    public void setStringProp(final String stringProp) {
        this.stringProp = stringProp;
    }

    public SlaveEntity getEntityProp() {
        return entityProp;
    }

    @Observable
    public void setEntityProp(final SlaveEntity entityProp) {
        this.entityProp = entityProp;
    }

    public List<SlaveEntity> getCollection() {
        return collection;
    }

    @Observable
    public void setCollection(final List<SlaveEntity> collection) {
        this.collection.clear();
        this.collection.addAll(collection);
    }
}
