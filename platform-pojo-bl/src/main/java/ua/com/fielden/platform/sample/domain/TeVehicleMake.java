package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITeVehicleMake.class)
public class TeVehicleMake extends AbstractEntity<String> {
   
    @IsProperty
    @Calculated
    private String c1;
    protected static final ExpressionModel c1_ = expr().prop("key").model();

    @IsProperty
    @Calculated
    private String c2;
    protected static final ExpressionModel c2_ = expr().prop("c1").model();

    @IsProperty
    @Calculated
    private String c3;
    protected static final ExpressionModel c3_ = expr().prop("c2").model();

    @IsProperty
    @Calculated
    private String c4;
    protected static final ExpressionModel c4_ = expr().prop("desc").model();

    @IsProperty
    @Calculated
    private String c5;
    protected static final ExpressionModel c5_ = expr().prop("c4").model();

    @IsProperty
    @Calculated
    private String c6;
    protected static final ExpressionModel c6_ = expr().prop("c5").model();

    @IsProperty
    @Calculated
    private String c7;
    protected static final ExpressionModel c7_ = expr().concat().prop("key").with().prop("desc").end().model();

    @IsProperty
    @Calculated
    private String c8;
    protected static final ExpressionModel c8_ = expr().concat().prop("c6").with().prop("c3").end().model();
    
    @IsProperty
    @Calculated
    private Money p9;
    protected static final ExpressionModel p9_ = expr().val(1).model();

    @IsProperty
    @Calculated
    private Money p8;
    protected static final ExpressionModel p8_ = expr().val(2).model();

    @IsProperty
    @Calculated
    private Money p7;
    protected static final ExpressionModel p7_ = expr().prop("p8").add().prop("p9").model();

    @Observable
    protected TeVehicleMake setP7(final Money p7) {
        this.p7 = p7;
        return this;
    }

    public Money getP7() {
        return p7;
    }

    @Observable
    protected TeVehicleMake setP8(final Money p8) {
        this.p8 = p8;
        return this;
    }

    public Money getP8() {
        return p8;
    }
    
    @Observable
    protected TeVehicleMake setP9(final Money p9) {
        this.p9 = p9;
        return this;
    }

    public Money getP9() {
        return p9;
    }

    @Observable
    protected TeVehicleMake setC1(final String c1) {
        this.c1 = c1;
        return this;
    }

    public String getC1() {
        return c1;
    }

    @Observable
    protected TeVehicleMake setC2(final String c2) {
        this.c2 = c2;
        return this;
    }

    public String getC2() {
        return c2;
    }

    @Observable
    protected TeVehicleMake setC3(final String c3) {
        this.c3 = c3;
        return this;
    }

    public String getC3() {
        return c3;
    }

    @Observable
    protected TeVehicleMake setC4(final String c4) {
        this.c4 = c4;
        return this;
    }

    public String getC4() {
        return c4;
    }

    @Observable
    protected TeVehicleMake setC5(final String c5) {
        this.c5 = c5;
        return this;
    }

    public String getC5() {
        return c5;
    }

    @Observable
    protected TeVehicleMake setC6(final String c6) {
        this.c6 = c6;
        return this;
    }

    public String getC6() {
        return c6;
    }
    
    @Observable
    protected TeVehicleMake setC7(final String c7) {
        this.c7 = c7;
        return this;
    }

    public String getC7() {
        return c7;
    }

    @Observable
    protected TeVehicleMake setC8(final String c8) {
        this.c8 = c8;
        return this;
    }

    public String getC8() {
        return c8;
    }
}