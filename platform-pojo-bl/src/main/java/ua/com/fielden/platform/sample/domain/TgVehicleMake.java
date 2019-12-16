package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITgVehicleMake.class)
public class TgVehicleMake extends AbstractEntity<String> {
    @IsProperty
    @Title(value = "Non-persisted prop", desc = "Desc")
    private String npProp;


    @Observable
    public TgVehicleMake setNpProp(final String npProp) {
        this.npProp = npProp;
        return this;
    }

    public String getNpProp() {
        return npProp;
    }

    @IsProperty
    @Title(value = "Competitor", desc = "Competitor")
    private TgVehicleMake competitor;

    @Observable
    public TgVehicleMake setCompetitor(final TgVehicleMake competitor) {
        this.competitor = competitor;
        return this;
    }

    public TgVehicleMake getCompetitor() {
        return competitor;
    }
    
    @IsProperty
    @Calculated
    private String c1;
    protected static final ExpressionModel c1_ = expr().prop("key").model();

    @Observable
    protected TgVehicleMake setC1(final String c1) {
        this.c1 = c1;
        return this;
    }

    public String getC1() {
        return c1;
    }
    
    @IsProperty
    @Calculated
    private String c2;
    protected static final ExpressionModel c2_ = expr().prop("c1").model();

    @Observable
    protected TgVehicleMake setC2(final String c2) {
        this.c2 = c2;
        return this;
    }

    public String getC2() {
        return c2;
    }
    
    @IsProperty
    @Calculated
    private String c3;
    protected static final ExpressionModel c3_ = expr().prop("c2").model();

    @Observable
    protected TgVehicleMake setC3(final String c3) {
        this.c3 = c3;
        return this;
    }

    public String getC3() {
        return c3;
    }
    
    @IsProperty
    @Calculated
    private String c4;
    protected static final ExpressionModel c4_ = expr().prop("desc").model();

    @Observable
    protected TgVehicleMake setC4(final String c4) {
        this.c4 = c4;
        return this;
    }

    public String getC4() {
        return c4;
    }
    
    @IsProperty
    @Calculated
    private String c5;
    protected static final ExpressionModel c5_ = expr().prop("c4").model();

    @Observable
    protected TgVehicleMake setC5(final String c5) {
        this.c5 = c5;
        return this;
    }

    public String getC5() {
        return c5;
    }
    
    @IsProperty
    @Calculated
    private String c6;
    protected static final ExpressionModel c6_ = expr().prop("c5").model();

    @Observable
    protected TgVehicleMake setC6(final String c6) {
        this.c6 = c6;
        return this;
    }

    public String getC6() {
        return c6;
    }
    
    @IsProperty
    @Calculated
    private String c7;
    protected static final ExpressionModel c7_ = expr().concat().prop("key").with().prop("desc").end().model();

    @Observable
    protected TgVehicleMake setC7(final String c7) {
        this.c7 = c7;
        return this;
    }

    public String getC7() {
        return c7;
    }

}