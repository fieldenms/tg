package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.sample.domain.definers.LocalDatesToUtcDefiner;
import ua.com.fielden.platform.sample.domain.definers.UtcDatesToLocalDefiner;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityWithTimeZoneDates.class)
@MapEntityTo
public class TgEntityWithTimeZoneDates extends AbstractPersistentEntity<String> {
    @IsProperty
    @MapTo
    @Title("Date Prop")
    @AfterChange(LocalDatesToUtcDefiner.class)
    private Date dateProp;
    
    @IsProperty
    @MapTo
    @Title("Date Prop UTC")
    @PersistentType(userType = IUtcDateTimeType.class)
    @AfterChange(UtcDatesToLocalDefiner.class)
    private Date datePropUtc;

    @Observable
    public TgEntityWithTimeZoneDates setDatePropUtc(final Date datePropUtc) {
        this.datePropUtc = datePropUtc;
        return this;
    }

    public Date getDatePropUtc() {
        return datePropUtc;
    }

    @Observable
    public TgEntityWithTimeZoneDates setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }

    public Date getDateProp() {
        return dateProp;
    }
}