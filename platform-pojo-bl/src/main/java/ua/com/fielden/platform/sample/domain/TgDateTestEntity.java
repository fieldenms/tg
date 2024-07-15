package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;

@KeyType(String.class)
@KeyTitle(value = "Date Test Entity Key", desc = "Date test entity key description")
@CompanionObject(ITgDateTestEntity.class)
@MapEntityTo
@DescTitle(value = "Date Test Entity Description", desc = "Date test entity extended description")
public class TgDateTestEntity extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @DateOnly
    @Dependent("toDateProp")
    @Title("From Date Property")
    private Date fromDateProp;

    @IsProperty
    @MapTo
    @DateOnly
    @Dependent("fromDateProp")
    @Title("To Date Property")
    private Date toDateProp;

    @Observable
    @LeProperty("toDateProp")
    public TgDateTestEntity setFromDateProp(final Date fromDateProp) {
        this.fromDateProp = fromDateProp;
        return this;
    }

    public Date getFromDateProp() {
        return fromDateProp;
    }

    @Observable
    @GeProperty("fromDateProp")
    public TgDateTestEntity setToDateProp(final Date toDateProp) {
        this.toDateProp = toDateProp;
        return this;
    }

    public Date getToDateProp() {
        return toDateProp;
    }
}
