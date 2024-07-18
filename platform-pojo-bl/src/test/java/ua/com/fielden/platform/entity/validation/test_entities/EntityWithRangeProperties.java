/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for testing of range property validators.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithRangeProperties extends AbstractEntity<String> {

    @IsProperty
    @Dependent("toInt")
    private Integer fromInt;
    @IsProperty
    @Dependent("fromInt")
    private Integer toInt;

    @IsProperty
    @Dependent("toNumber")
    private BigDecimal fromNumber;
    @IsProperty
    @Dependent("fromNumber")
    private BigDecimal toNumber;

    @IsProperty
    @Dependent("toDate")
    private Date fromDate;
    @IsProperty
    @Dependent("fromDate")
    private Date toDate;

    @IsProperty
    @Dependent("toDateTime")
    private DateTime fromDateTime;
    @IsProperty
    @Dependent("fromDateTime")
    private DateTime toDateTime;

    @IsProperty
    @Dependent("toMoney")
    private Money fromMoney;
    @IsProperty
    @Dependent("fromMoney")
    private Money toMoney;

    public Integer getFromInt() {
        return fromInt;
    }

    @Observable
    @LeProperty("toInt")
    public void setFromInt(final Integer fromInt) {
        this.fromInt = fromInt;
    }

    public Integer getToInt() {
        return toInt;
    }

    @Observable
    @GeProperty("fromInt")
    public void setToInt(final Integer toInt) {
        this.toInt = toInt;
    }

    public Date getFromDate() {
        return fromDate;
    }

    @Observable
    @LeProperty("toDate")
    public void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    @Observable
    @GeProperty("fromDate")
    public void setToDate(final Date toDate) {
        this.toDate = toDate;
    }

    public Money getFromMoney() {
        return fromMoney;
    }

    @Observable
    @LeProperty("toMoney")
    public void setFromMoney(final Money fromMoney) {
        this.fromMoney = fromMoney;
    }

    public Money getToMoney() {
        return toMoney;
    }

    @Observable
    @GeProperty("fromMoney")
    public void setToMoney(final Money toMoney) {
        this.toMoney = toMoney;
    }

    public BigDecimal getFromNumber() {
        return fromNumber;
    }

    @Observable
    @LeProperty("toNumber")
    public void setFromNumber(final BigDecimal fromNumber) {
        this.fromNumber = fromNumber;
    }

    public BigDecimal getToNumber() {
        return toNumber;
    }

    @Observable
    @GeProperty("fromNumber")
    public void setToNumber(final BigDecimal toNumber) {
        this.toNumber = toNumber;
    }

    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    @Observable
    @LeProperty("toDateTime")
    public void setFromDateTime(final DateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public DateTime getToDateTime() {
        return toDateTime;
    }

    @Observable
    @GeProperty("fromDateTime")
    public void setToDateTime(final DateTime toDateTime) {
        this.toDateTime = toDateTime;
    }

}
