package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test entity that should be used as singleton with properties that are named after its values and be pre-populated for all EQL database interaction unit tests.
 *
 * @author TG Team
 *
 */

@KeyType(String.class)
@MapEntityTo
@Ignore
@CompanionObject(TeNamedValuesVectorCo.class)
public class TeNamedValuesVector extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Date dateOf20010911;

    @IsProperty
    @MapTo
    private Date dateAndTimeOf20010911084640;

    @IsProperty
    @MapTo
    private Date dateOfNullValue;

    @IsProperty
    @MapTo
    private String stringOfSadEvent;

    @IsProperty
    @MapTo
    private String uppercasedStringOfAbc;

    @IsProperty
    @MapTo
    private String lowercasedStringOfAbc;

    @IsProperty
    @MapTo
    private String stringOfNullValue;

    @IsProperty
    @MapTo
    private Integer integerOfZero;

    @IsProperty
    @MapTo
    private Integer integerOfNullValue;


    public Integer getIntegerOfNullValue() {
        return integerOfNullValue;
    }

    @Observable
    public TeNamedValuesVector setIntegerOfNullValue(final Integer integerOfNullValue) {
        this.integerOfNullValue = integerOfNullValue;
        return this;
    }

    public Integer getIntegerOfZero() {
        return integerOfZero;
    }

    @Observable
    public TeNamedValuesVector setIntegerOfZero(final Integer integerOfZero) {
        this.integerOfZero = integerOfZero;
        return this;
    }

    public String getStringOfNullValue() {
        return stringOfNullValue;
    }

    @Observable
    public TeNamedValuesVector setStringOfNullValue(final String stringOfNullValue) {
        this.stringOfNullValue = stringOfNullValue;
        return this;
    }

    public String getStringOfSadEvent() {
        return stringOfSadEvent;
    }

    @Observable
    public TeNamedValuesVector setStringOfSadEvent(final String stringOfSadEvent) {
        this.stringOfSadEvent = stringOfSadEvent;
        return this;
    }

    public String getUppercasedStringOfAbc() {
        return uppercasedStringOfAbc;
    }

    @Observable
    public TeNamedValuesVector setUppercasedStringOfAbc(final String uppercasedStringOfAbc) {
        this.uppercasedStringOfAbc = uppercasedStringOfAbc;
        return this;
    }

    public String getLowercasedStringOfAbc() {
        return lowercasedStringOfAbc;
    }

    @Observable
    public TeNamedValuesVector setLowercasedStringOfAbc(final String lowercasedStringOfAbc) {
        this.lowercasedStringOfAbc = lowercasedStringOfAbc;
        return this;
    }

    public Date getDateOf20010911() {
        return dateOf20010911;
    }

    @Observable
    public TeNamedValuesVector setDateOf20010911(final Date dateOf20010911) {
        this.dateOf20010911 = dateOf20010911;
        return this;
    }

    public Date getDateAndTimeOf20010911084640() {
        return dateAndTimeOf20010911084640;
    }

    @Observable
    public TeNamedValuesVector setDateAndTimeOf20010911084640(final Date dateAndTimeOf20010911084640) {
        this.dateAndTimeOf20010911084640 = dateAndTimeOf20010911084640;
        return this;
    }

    public Date getDateOfNullValue() {
        return dateOfNullValue;
    }

    @Observable
    public TeNamedValuesVector setDateOfNullValue(final Date dateOfNullValue) {
        this.dateOfNullValue = dateOfNullValue;
        return this;
    }
}