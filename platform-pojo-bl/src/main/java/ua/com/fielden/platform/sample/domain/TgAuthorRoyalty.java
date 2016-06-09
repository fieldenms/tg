package ua.com.fielden.platform.sample.domain;

import java.util.Date;

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
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.types.Money;

/**
 * One-2-Many entity object.
 * 
 * @author Developers
 * 
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgAuthorRoyalty.class)
@MapEntityTo
public class TgAuthorRoyalty extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @Title("Authorship being rewarded")
    @MapTo
    @CompositeKeyMember(1)
    private TgAuthorship authorship;

    @IsProperty
    @MapTo
    @Title(value = "Payment date", desc = "Payment date")
    @CompositeKeyMember(2)
    private Date paymentDate;

    @IsProperty
    @MapTo
    @Title("Payment")
    private Money payment;

    @Observable
    public TgAuthorRoyalty setPayment(final Money payment) {
        this.payment = payment;
        return this;
    }

    public Money getPayment() {
        return payment;
    }

    @Observable
    public TgAuthorRoyalty setPaymentDate(final Date paymentDate) {
        this.paymentDate = paymentDate;
        return this;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    @Observable
    @EntityExists(TgAuthorship.class)
    public TgAuthorRoyalty setAuthorship(final TgAuthorship value) {
        this.authorship = value;
        return this;
    }

    public TgAuthorship getAuthorship() {
        return authorship;
    }

}