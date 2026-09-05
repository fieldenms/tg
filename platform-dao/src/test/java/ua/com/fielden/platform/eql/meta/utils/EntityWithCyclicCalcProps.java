package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

@KeyType(String.class)
@MapEntityTo
public class EntityWithCyclicCalcProps extends AbstractEntity<String> {

    @IsProperty
    @Readonly
    @Calculated
    private Integer i1;
    protected static final ExpressionModel i1_ = expr().val(1).add().prop("i2").model();

    @IsProperty
    @Readonly
    @Calculated
    private Integer i2;
    protected static final ExpressionModel i2_ = expr().val(2).add().prop("i1").model();

    @Observable
    protected EntityWithCyclicCalcProps setI2(final Integer i2) {
        this.i2 = i2;
        return this;
    }

    public Integer getI2() {
        return i2;
    }

    @Observable
    protected EntityWithCyclicCalcProps setI1(final Integer i1) {
        this.i1 = i1;
        return this;
    }

    public Integer getI1() {
        return i1;
    }


}
