package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/// A test entity with a chain of dependent calculated properties but no cycle: `top -> mid -> bottom`.
/// Serves as the positive control for the cycle-detection tests.
///
@KeyType(String.class)
@MapEntityTo
public class EntityWithAcyclicCalcProps extends AbstractEntity<String> {

    @IsProperty
    @Readonly
    @Calculated
    private Integer top;
    protected static final ExpressionModel top_ = expr().prop("mid").add().val(1).model();

    @IsProperty
    @Readonly
    @Calculated
    private Integer mid;
    protected static final ExpressionModel mid_ = expr().prop("bottom").add().val(1).model();

    @IsProperty
    @Readonly
    @Calculated
    private Integer bottom;
    protected static final ExpressionModel bottom_ = expr().val(0).model();

    @Observable
    protected EntityWithAcyclicCalcProps setTop(final Integer top) {
        this.top = top;
        return this;
    }

    public Integer getTop() {
        return top;
    }

    @Observable
    protected EntityWithAcyclicCalcProps setMid(final Integer mid) {
        this.mid = mid;
        return this;
    }

    public Integer getMid() {
        return mid;
    }

    @Observable
    protected EntityWithAcyclicCalcProps setBottom(final Integer bottom) {
        this.bottom = bottom;
        return this;
    }

    public Integer getBottom() {
        return bottom;
    }

}
