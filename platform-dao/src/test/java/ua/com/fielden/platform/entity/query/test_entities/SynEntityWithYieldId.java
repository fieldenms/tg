package ua.com.fielden.platform.entity.query.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Synthetic entity that yields into property {@code id}.
 */
@KeyType(String.class)
public class SynEntityWithYieldId extends AbstractEntity<String> {

    // @formatter:off
    protected static final EntityResultQueryModel<SynEntityWithYieldId> model_ =
            select()
                .yield().val(1).as("id")
                .modelAsEntity(SynEntityWithYieldId.class);
    // @formatter:on

}
