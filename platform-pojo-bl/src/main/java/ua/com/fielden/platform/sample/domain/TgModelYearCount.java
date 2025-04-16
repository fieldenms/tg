package ua.com.fielden.platform.sample.domain;

import java.math.BigInteger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
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
@CompanionObject(ITgModelYearCount.class)
public class TgModelYearCount extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Model", desc = "Model")
    @CompositeKeyMember(1)
    private TgVehicleModel model;

    @IsProperty
    @MapTo
    @Title(value = "Commision year", desc = "Commision year")
    @CompositeKeyMember(2)
    private Long year;

    @IsProperty
    @MapTo
    @Title(value = "Count", desc = "Count per vehicle")
    private Long count;

    @Observable
    public TgModelYearCount setModel(final TgVehicleModel model) {
        this.model = model;
        return this;
    }

    public TgVehicleModel getModel() {
        return model;
    }

    @Observable
    public TgModelYearCount setYear(final Long year) {
        this.year = year;
        return this;
    }

    public Long getYear() {
        return year;
    }

    @Observable
    public TgModelYearCount setCount(final Long count) {
        this.count = count;
        return this;
    }

    public Long getCount() {
        return count;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgModelYearCount() {
    }
}
