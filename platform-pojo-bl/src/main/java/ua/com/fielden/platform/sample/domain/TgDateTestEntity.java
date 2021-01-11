package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgDateTestEntity.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgDateTestEntity extends AbstractEntity<String> {

    @IsProperty
    @Title("From Date Prop Date Only")
    @DateOnly
    private Date fromDatePropDateOnly;

    @IsProperty
    @Title("To Date Prop Date Only")
    @DateOnly
    private Date toDatePropDateOnly;

    @Observable
    public TgDateTestEntity setFromDatePropDateOnly(final Date fromDatePropDateOnly) {
        this.fromDatePropDateOnly = fromDatePropDateOnly;
        return this;
    }

    public Date getFromDatePropDateOnly() {
        return fromDatePropDateOnly;
    }

    @Observable
    public TgDateTestEntity setToDatePropDateOnly(final Date toDatePropDateOnly) {
        this.toDatePropDateOnly = toDatePropDateOnly;
        return this;
    }

    public Date getToDatePropDateOnly() {
        return toDatePropDateOnly;
    }

}
