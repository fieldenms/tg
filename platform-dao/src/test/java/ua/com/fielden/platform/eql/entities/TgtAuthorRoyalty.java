package ua.com.fielden.platform.eql.entities;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * One-2-Many entity object.
 * 
 * @author Developers
 * 
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtAuthorRoyalty extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgtAuthorship authorship;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Date paymentDate;

    @IsProperty
    @MapTo
    private BigDecimal payment;

    @Observable
    public TgtAuthorRoyalty setPayment(final BigDecimal payment) {
        this.payment = payment;
        return this;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    @Observable
    public TgtAuthorRoyalty setPaymentDate(final Date paymentDate) {
        this.paymentDate = paymentDate;
        return this;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    @Observable
    public TgtAuthorRoyalty setAuthorship(final TgtAuthorship value) {
        this.authorship = value;
        return this;
    }

    public TgtAuthorship getAuthorship() {
        return authorship;
    }

}