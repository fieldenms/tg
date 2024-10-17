package ua.com.fielden.platform.entity.query.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Synthetic entity that doesn't yield into property {@code id}.
 */
@KeyType(String.class)
public class SynEntityWithoutYieldId extends AbstractEntity<String> {

    // @formatter:off
    protected static final EntityResultQueryModel<SynEntityWithoutYieldId> model_ =
            select()
                .yield().val(1).as("number")
                .modelAsEntity(SynEntityWithoutYieldId.class);
    // @formatter:on

    @IsProperty
    @MapTo
    private Integer number;

    public Integer getNumber() {
        return number;
    }

    @Observable
    public SynEntityWithoutYieldId setNumber(final Integer number) {
        this.number = number;
        return this;
    }

}
