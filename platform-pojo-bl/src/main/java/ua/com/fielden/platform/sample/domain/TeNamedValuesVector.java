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
    private Date dateOfNullValue;

    public Date getDateOf20010911() {
        return dateOf20010911;
    }

    @Observable
    public TeNamedValuesVector setDateOf20010911(final Date dateOf20010911) {
        this.dateOf20010911 = dateOf20010911;
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