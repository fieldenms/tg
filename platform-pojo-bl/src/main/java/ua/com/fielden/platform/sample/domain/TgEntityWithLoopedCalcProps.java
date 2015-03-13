package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityWithLoopedCalcProps.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgEntityWithLoopedCalcProps extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Integer intProp;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private Integer calc1;
    private static ExpressionModel calc1_ = expr().prop("intProp").add().prop("calc2").model();
    
    
    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private Integer calc2;
    private static ExpressionModel calc2_ = expr().prop("calc3").mult().val(10).model();
    
    
    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private Integer calc3;
    private static ExpressionModel calc3_ = expr().prop("calc1").sub().prop("intProp").model();
    

    @Observable
    protected TgEntityWithLoopedCalcProps setCalc3(final Integer calc3) {
        this.calc3 = calc3;
        return this;
    }

    public Integer getCalc3() {
        return calc3;
    }

    

    


    @Observable
    protected TgEntityWithLoopedCalcProps setCalc2(final Integer calc2) {
        this.calc2 = calc2;
        return this;
    }

    public Integer getCalc2() {
        return calc2;
    }

    

    


    @Observable
    protected TgEntityWithLoopedCalcProps setCalc1(final Integer calc1) {
        this.calc1 = calc1;
        return this;
    }

    public Integer getCalc1() {
        return calc1;
    }

    

    

    
    @Observable
    public TgEntityWithLoopedCalcProps setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    

    
}