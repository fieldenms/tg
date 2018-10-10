package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgCentreInvokerWithCentreContext.class)
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgCentreInvokerWithCentreContext  extends AbstractFunctionalEntityWithCentreContext<String> {
    @IsProperty
    @Title("Crit Only BigDecimal Prop Criterion")
    private BigDecimal critOnlyBigDecimalPropCriterion;
    
    @IsProperty
    @Title("BigDecimal Prop From Criterion")
    private BigDecimal bigDecimalPropFromCriterion;

    @Observable
    public TgCentreInvokerWithCentreContext setBigDecimalPropFromCriterion(final BigDecimal bigDecimalPropFromCriterion) {
        this.bigDecimalPropFromCriterion = bigDecimalPropFromCriterion;
        return this;
    }

    public BigDecimal getBigDecimalPropFromCriterion() {
        return bigDecimalPropFromCriterion;
    }

    @Observable
    public TgCentreInvokerWithCentreContext setCritOnlyBigDecimalPropCriterion(final BigDecimal critOnlyBigDecimalPropCriterion) {
        this.critOnlyBigDecimalPropCriterion = critOnlyBigDecimalPropCriterion;
        return this;
    }

    public BigDecimal getCritOnlyBigDecimalPropCriterion() {
        return critOnlyBigDecimalPropCriterion;
    }
}