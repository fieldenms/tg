package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class MasterSyntheticEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    private static EntityResultQueryModel<MasterSyntheticEntity> model_ = select(TgVehicleMake.class).yield().prop("key").as("key").modelAsEntity(MasterSyntheticEntity.class);

    protected MasterSyntheticEntity() {
    }
}