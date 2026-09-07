package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/// A test entity whose two calculated *component-typed* ([Money]) properties depend on each other.
///
/// A component-typed query source item never carries an expression of its own -- the expression lands on its
/// sub-items -- so such properties are enumerated as `costA.amount`, `costB.amount`, and so on.
/// This entity covers that enumeration path, which the scalar cycle fixtures do not reach.
///
@KeyType(String.class)
@MapEntityTo
public class EntityWithCyclicCalcComponentProps extends AbstractEntity<String> {

    @IsProperty
    @Readonly
    @Calculated
    private Money costA;
    protected static final ExpressionModel costA_ = expr().prop("costB").model();

    @IsProperty
    @Readonly
    @Calculated
    private Money costB;
    protected static final ExpressionModel costB_ = expr().prop("costA").model();

    @Observable
    protected EntityWithCyclicCalcComponentProps setCostA(final Money costA) {
        this.costA = costA;
        return this;
    }

    public Money getCostA() {
        return costA;
    }

    @Observable
    protected EntityWithCyclicCalcComponentProps setCostB(final Money costB) {
        this.costB = costB;
        return this;
    }

    public Money getCostB() {
        return costB;
    }

}
