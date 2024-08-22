package ua.com.fielden.platform.sample.domain;

import java.math.BigInteger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(TgVehicleModel.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgModelCount.class)
public class TgModelCount extends AbstractEntity<TgVehicleModel> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Count", desc = "Vehicle Count per Model")
    private Long count;

    @Observable
    public TgModelCount setCount(final Long count) {
        this.count = count;
        return this;
    }

    public Long getCount() {
        return count;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgModelCount() {
    }
}
