package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgVehicle extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo()
    private Date initDate;

    @IsProperty @MapTo()
    private TgVehicle replacedBy;

    @IsProperty @MapTo()
    private TgOrgUnit5 station;

    @IsProperty
    @MapTo
    @Title(value = "Model", desc = "Model")
    private TgVehicleModel model;

    @IsProperty
    @MapTo
    @Title(value = "Price", desc = "Price")
    private Money price;

    @Observable
    public TgVehicle setPrice(final Money price) {
	this.price = price;
	return this;
    }

    public Money getPrice() {
	return price;
    }

    @Observable
    public TgVehicle setModel(final TgVehicleModel model) {
	this.model = model;
	return this;
    }

    public TgVehicleModel getModel() {
	return model;
    }

    @Observable
    public TgVehicle setStation(final TgOrgUnit5 station) {
	this.station = station;
	return this;
    }

    public TgOrgUnit5 getStation() {
	return station;
    }

    @IsProperty @MapTo(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
    }
}
