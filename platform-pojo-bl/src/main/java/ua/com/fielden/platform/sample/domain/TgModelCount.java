package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(TgVehicleModel.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgModelCount.class)
public class TgModelCount extends AbstractEntity<TgVehicleModel> {
    private static final long serialVersionUID = 1L;

//    @IsProperty
//    @MapTo
//    @Title(value = "Count", desc = "Vehicle Count per Model")
//    private BigInteger count;
//
//    @Observable
//    public TgModelCount setCount(final BigInteger count) {
//        this.count = count;
//        return this;
//    }
//
//    public BigInteger getCount() {
//        return count;
//    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgModelCount() {
    }
}