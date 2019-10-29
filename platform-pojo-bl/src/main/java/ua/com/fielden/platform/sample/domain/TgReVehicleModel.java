package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * This is a simple synthetic entity that is based on a persistent entity {@link TgVehicleModel} and yields all inherited properties.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ITgVehicleModel.class)
public class TgReVehicleModel extends TgVehicleModel {
    protected static final EntityResultQueryModel<TgReVehicleModel> model_ = select(TgVehicleModel.class)
            .yieldAll()
            .yield().caseWhen().val(true).eq().val(true).then().val(42).otherwise().val(42).endAsInt().as("intProp")
            .modelAsEntity(TgReVehicleModel.class);

    @IsProperty
    @CritOnly(Type.MULTI)
    @Title("Crit prop")
    private Integer intCritProp;

    @IsProperty
    @Title("Extra prop")
    private Integer intProp;

    @IsProperty
    @Title("Not yielded prop")
    private Integer noYieldIntProp;

    @Observable
    public TgReVehicleModel setNoYieldIntProp(final Integer noYieldIntProp) {
        this.noYieldIntProp = noYieldIntProp;
        return this;
    }

    public Integer getNoYieldIntProp() {
        return noYieldIntProp;
    }
    
    @Observable
    public TgReVehicleModel setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public TgReVehicleModel setIntCritProp(final Integer intCritProp) {
        this.intCritProp = intCritProp;
        return this;
    }

    public Integer getIntCritProp() {
        return intCritProp;
    }

}
