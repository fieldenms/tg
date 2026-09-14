package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/// A test entity whose calculated property refers to itself, forming a self-cycle.
///
@KeyType(String.class)
@MapEntityTo
public class EntityWithSelfReferentialCalcProp extends AbstractEntity<String> {

    @IsProperty
    @Readonly
    @Calculated
    private Integer selfRef;
    protected static final ExpressionModel selfRef_ = expr().prop("selfRef").add().val(1).model();

    @Observable
    protected EntityWithSelfReferentialCalcProp setSelfRef(final Integer selfRef) {
        this.selfRef = selfRef;
        return this;
    }

    public Integer getSelfRef() {
        return selfRef;
    }

}
