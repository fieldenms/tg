package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// This is a simple synthetic entity that is based on activatable persistent entity [TgActivatableVehicleModel] and yields all inherited properties.
///
public class TgReActivatableVehicleModel extends TgActivatableVehicleModel {

    protected static final EntityResultQueryModel<TgReActivatableVehicleModel> model_ = select(TgActivatableVehicleModel.class)
            .yieldAll()
            .yield().caseWhen().val(true).eq().val(true).then().val(42).otherwise().val(42).endAsInt().as("intProp")
            .modelAsEntity(TgReActivatableVehicleModel.class);

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
    public TgReActivatableVehicleModel setNoYieldIntProp(final Integer noYieldIntProp) {
        this.noYieldIntProp = noYieldIntProp;
        return this;
    }

    public Integer getNoYieldIntProp() {
        return noYieldIntProp;
    }
    
    @Observable
    public TgReActivatableVehicleModel setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public TgReActivatableVehicleModel setIntCritProp(final Integer intCritProp) {
        this.intCritProp = intCritProp;
        return this;
    }

    public Integer getIntCritProp() {
        return intCritProp;
    }

}
