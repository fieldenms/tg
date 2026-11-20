package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with a simple `String` key and a spread of basic property kinds.
/// Used to verify generation of table and primary-key DDL.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_Simple extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Required
    private String requiredStr;

    @IsProperty
    @MapTo
    private Integer optionalInt;

    @IsProperty
    @MapTo
    private boolean active;

    @IsProperty
    @MapTo("CUSTOM_COLUMN")
    private String withCustomColumnName;

    @IsProperty
    @MapTo(defaultValue = "0")
    private Integer withDefaultValue;

    public String getRequiredStr() {
        return requiredStr;
    }

    @Observable
    public Entity_Simple setRequiredStr(final String requiredStr) {
        this.requiredStr = requiredStr;
        return this;
    }

    public Integer getOptionalInt() {
        return optionalInt;
    }

    @Observable
    public Entity_Simple setOptionalInt(final Integer optionalInt) {
        this.optionalInt = optionalInt;
        return this;
    }

    public boolean getActive() {
        return active;
    }

    @Observable
    public Entity_Simple setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public String getWithCustomColumnName() {
        return withCustomColumnName;
    }

    @Observable
    public Entity_Simple setWithCustomColumnName(final String withCustomColumnName) {
        this.withCustomColumnName = withCustomColumnName;
        return this;
    }

    public Integer getWithDefaultValue() {
        return withDefaultValue;
    }

    @Observable
    public Entity_Simple setWithDefaultValue(final Integer withDefaultValue) {
        this.withDefaultValue = withDefaultValue;
        return this;
    }

}
