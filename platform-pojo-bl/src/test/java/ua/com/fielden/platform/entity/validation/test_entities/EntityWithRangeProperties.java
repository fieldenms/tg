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
    @LeProperty("toInt")
    private Integer fromInt;
    @IsProperty
    @Dependent("fromInt")
    @GeProperty("fromInt")
    private Integer toInt;

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
