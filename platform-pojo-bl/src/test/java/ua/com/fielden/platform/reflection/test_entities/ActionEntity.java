package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/// An action entity for testing purposes.
/// It includes properties defined as `@MapTo` and `@Calculated` to test various edge cases,
/// even those such definitions are nonsensical in practice.
///
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
public class ActionEntity extends AbstractFunEntityForDataExport<String> {

    @IsProperty
    @MapTo
    @Title
    private Integer count;

    @IsProperty
    @Readonly
    @Calculated
    @Title
    private Integer calculated;
    protected static final ExpressionModel calculated_ = expr().val(null).model();

    @IsProperty
    @Title
    private TgCategory plainCategory;

    @IsProperty
    @Title
    private Union union;

    public Union getUnion() {
        return union;
    }

    @Observable
    public ActionEntity setUnion(final Union union) {
        this.union = union;
        return this;
    }

    public TgCategory getPlainCategory() {
        return plainCategory;
    }

    @Observable
    public ActionEntity setPlainCategory(final TgCategory plainCategory) {
        this.plainCategory = plainCategory;
        return this;
    }

    @Observable
    protected ActionEntity setCalculated(final Integer calculated) {
        this.calculated = calculated;
        return this;
    }

    public Integer getCalculated() {
        return calculated;
    }

    @Observable
    public ActionEntity setCount(final Integer count) {
        this.count = count;
        return this;
    }

    public Integer getCount() {
        return count;
    }
}