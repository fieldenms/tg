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
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgDateTestEntity.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgDateTestEntity extends AbstractEntity<String> {

    @IsProperty
    @Title("From Date Prop")
    @DateOnly
    @MapTo
    private Date fromDateProp;

    @IsProperty
    @Title("To Date Prop")
    @DateOnly
    @MapTo
    private Date toDateProp;

    @Observable
    public TgDateTestEntity setFromDateProp(final Date fromDateProp) {
        this.fromDateProp = fromDateProp;
        return this;
    }

    public Date getFromDateProp() {
        return fromDateProp;
    }

    @Observable
    public TgDateTestEntity setToDateProp(final Date toDatePropDateOnly) {
        this.toDateProp = toDatePropDateOnly;
        return this;
    }

    public Date getToDateProp() {
        return toDateProp;
    }

}
