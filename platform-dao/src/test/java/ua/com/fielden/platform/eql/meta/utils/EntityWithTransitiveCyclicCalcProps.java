package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/// A test entity with a transitive cycle across three calculated properties: `alpha -> beta -> gamma -> alpha`.
///
@KeyType(String.class)
@MapEntityTo
public class EntityWithTransitiveCyclicCalcProps extends AbstractEntity<String> {

    @IsProperty
    @Readonly
    @Calculated
    private Integer alpha;
    protected static final ExpressionModel alpha_ = expr().prop("beta").add().val(1).model();

    @IsProperty
    @Readonly
    @Calculated
    private Integer beta;
    protected static final ExpressionModel beta_ = expr().prop("gamma").add().val(1).model();

    @IsProperty
    @Readonly
    @Calculated
    private Integer gamma;
    protected static final ExpressionModel gamma_ = expr().prop("alpha").add().val(1).model();

    @Observable
    protected EntityWithTransitiveCyclicCalcProps setAlpha(final Integer alpha) {
        this.alpha = alpha;
        return this;
    }

    public Integer getAlpha() {
        return alpha;
    }

    @Observable
    protected EntityWithTransitiveCyclicCalcProps setBeta(final Integer beta) {
        this.beta = beta;
        return this;
    }

    public Integer getBeta() {
        return beta;
    }

    @Observable
    protected EntityWithTransitiveCyclicCalcProps setGamma(final Integer gamma) {
        this.gamma = gamma;
        return this;
    }

    public Integer getGamma() {
        return gamma;
    }

}
