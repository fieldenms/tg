package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

@CompanionObject(TgReBogieWithHighLoadCo.class)
public class TgReBogieWithHighLoad extends TgBogie {

    protected static final EntityResultQueryModel<TgReBogieWithHighLoad> model_ = select(TgBogie.class)
            .where().prop("bogieClass.tonnage").ge().val(20)
            .yieldAll()
            .modelAsEntity(TgReBogieWithHighLoad.class);

    @IsProperty
    @Readonly
    @Calculated
    @Title
    private Integer calculated;
    protected static final ExpressionModel calculated_ = expr().val(42).model();

    @Observable
    protected TgReBogieWithHighLoad setCalculated(final Integer calculated) {
        this.calculated = calculated;
        return this;
    }

    public Integer getCalculated() {
        return calculated;
    }

}