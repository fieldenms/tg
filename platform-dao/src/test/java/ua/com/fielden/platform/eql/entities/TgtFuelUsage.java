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
import ua.com.fielden.platform.entity.annotation.Required;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtFuelUsage extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgtVehicle vehicle;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Date date;

    @IsProperty
    @Required
    @MapTo
    private BigDecimal qty;


    public BigDecimal getQty() {
        return qty;
    }

    public TgtVehicle getVehicle() {
        return vehicle;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public void setVehicle(final TgtVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    @Observable
    public TgtFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }
}