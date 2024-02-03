package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@CompanionObject(TgReBogieWithHighLoadCo.class)
public class TgReBogieWithHighLoad extends TgBogie {
    protected static final EntityResultQueryModel<TgReBogieWithHighLoad> model_ = select(TgBogie.class)
            .where().prop("bogieClass.tonnage").ge().val(20)
            .yieldAll()
            .modelAsEntity(TgReBogieWithHighLoad.class);
}