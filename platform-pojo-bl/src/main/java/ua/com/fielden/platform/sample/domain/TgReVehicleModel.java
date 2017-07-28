package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * This is a simple synthetic entity that is based on a persistent entity {@link TgVehicleModel} and yields all inherited properties.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ITgVehicleModel.class)
public class TgReVehicleModel extends TgVehicleModel {
    protected static final EntityResultQueryModel<TgReVehicleModel> model_ = select(TgVehicleModel.class).yieldAll().modelAsEntity(TgReVehicleModel.class);
}
