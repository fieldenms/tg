/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import org.joda.time.DateTime;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for testing of range property validators.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithRangeProperties extends AbstractEntity<String> {

    @IsProperty
    @Dependent("toInt")
    @LeProperty("toInt")
    private Integer fromInt;
    @IsProperty
    @Dependent("fromInt")
    @GeProperty("fromInt")
    private Integer toInt;

    @IsProperty
    @Dependent("toIntStrict")
    @LeProperty(value = "toIntStrict", lt = true)
    private Integer fromIntStrict;

    @IsProperty
    @Dependent("fromIntStrict")
    @GeProperty(value = "fromIntStrict", gt = true)
    private Integer toIntStrict;

    @IsProperty
    @Dependent("toNumber")
    @LeProperty("toNumber")
    private BigDecimal fromNumber;
    @IsProperty
    @Dependent("fromNumber")
    @GeProperty("fromNumber")
    private BigDecimal toNumber;

    @IsProperty
    @Dependent("toDate")
    @LeProperty("toDate")
    private Date fromDate;
    @IsProperty
    @GeProperty("fromDate")
    @Dependent("fromDate")
    private Date toDate;

    @IsProperty
    @Dependent("toDateStrict")
    @LeProperty(value = "toDateStrict", lt = true)
    private Date fromDateStrict;

    @IsProperty
    @Dependent("fromDateStrict")
    @GeProperty(value = "fromDateStrict", gt = true)
    private Date toDateStrict;

    @IsProperty
    @Dependent("toDateTime")
    @LeProperty("toDateTime")
    private DateTime fromDateTime;
    @IsProperty
    @Dependent("fromDateTime")
    @GeProperty("fromDateTime")
    private DateTime toDateTime;

    @IsProperty
    @LeProperty("toMoney")
    @Dependent("toMoney")
    private Money fromMoney;
    @IsProperty
    @GeProperty("fromMoney")
    @Dependent("fromMoney")
    private Money toMoney;

    @IsProperty
    @TimeOnly
    @Dependent("toDateTimeOnly")
    @LeProperty("toDateTimeOnly")
    private Date fromDateTimeOnly;

    @IsProperty
    @TimeOnly
    @Dependent("fromDateTimeOnly")
    @GeProperty("fromDateTimeOnly")
    private Date toDateTimeOnly;

    @IsProperty
    @DateOnly
    @Dependent("toDateDateOnly")
    @LeProperty("toDateDateOnly")
    private Date fromDateDateOnly;

    @IsProperty
    @DateOnly
    @Dependent("fromDateDateOnly")
    @GeProperty("fromDateDateOnly")
    private Date toDateDateOnly;

    @Observable
    public EntityWithRangeProperties setFromDateStrict(final Date fromDateStrict) {
        this.fromDateStrict = fromDateStrict;
        return this;
    }

    public Date getFromDateStrict() {
        return fromDateStrict;
    }

    @Observable
    public EntityWithRangeProperties setToDateStrict(final Date toDateStrict) {
        this.toDateStrict = toDateStrict;
        return this;
    }

    public Date getToDateStrict() {
        return toDateStrict;
    }

    @Observable
    public EntityWithRangeProperties setFromIntStrict(final Integer fromIntStrict) {
        this.fromIntStrict = fromIntStrict;
        return this;
    }

    public Integer getFromIntStrict() {
        return fromIntStrict;
    }

    @Observable
    public EntityWithRangeProperties setToIntStrict(final Integer toIntStrict) {
        this.toIntStrict = toIntStrict;
        return this;
    }

    public Integer getToIntStrict() {
        return toIntStrict;
    }

    @Observable
    public EntityWithRangeProperties setFromDateDateOnly(final Date fromDateDateOnly) {
        this.fromDateDateOnly = fromDateDateOnly;
        return this;
    }

    public Date getFromDateDateOnly() {
        return fromDateDateOnly;
    }

    @Observable
    public EntityWithRangeProperties setToDateDateOnly(final Date toDateDateOnly) {
        this.toDateDateOnly = toDateDateOnly;
        return this;
    }

    public Date getToDateDateOnly() {
        return toDateDateOnly;
    }

    @Observable
    public EntityWithRangeProperties setFromDateTimeOnly(final Date fromDateTimeOnly) {
        this.fromDateTimeOnly = fromDateTimeOnly;
        return this;
    }

    public Date getFromDateTimeOnly() {
        return fromDateTimeOnly;
    }

    @Observable
    public EntityWithRangeProperties setToDateTimeOnly(final Date toDateTimeOnly) {
        this.toDateTimeOnly = toDateTimeOnly;
        return this;
    }

    public Date getToDateTimeOnly() {
        return toDateTimeOnly;
    }

    public Integer getFromInt() {
        return fromInt;
    }

    @Observable
    public void setFromInt(final Integer fromInt) {
        this.fromInt = fromInt;
    }

    public Integer getToInt() {
        return toInt;
    }

    @Observable
    public void setToInt(final Integer toInt) {
        this.toInt = toInt;
    }

    public Date getFromDate() {
        return fromDate;
    }

    @Observable
    public void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    @Observable
    public void setToDate(final Date toDate) {
        this.toDate = toDate;
    }

    public Money getFromMoney() {
        return fromMoney;
    }

    @Observable
    public void setFromMoney(final Money fromMoney) {
        this.fromMoney = fromMoney;
    }

    public Money getToMoney() {
        return toMoney;
    }

    @Observable
    public void setToMoney(final Money toMoney) {
        this.toMoney = toMoney;
    }

    public BigDecimal getFromNumber() {
        return fromNumber;
    }

    @Observable
    public void setFromNumber(final BigDecimal fromNumber) {
        this.fromNumber = fromNumber;
    }

    public BigDecimal getToNumber() {
        return toNumber;
    }

    @Observable
    public void setToNumber(final BigDecimal toNumber) {
        this.toNumber = toNumber;
    }

    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    @Observable
    public void setFromDateTime(final DateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public DateTime getToDateTime() {
        return toDateTime;
    }

    @Observable
    public void setToDateTime(final DateTime toDateTime) {
        this.toDateTime = toDateTime;
    }

}
