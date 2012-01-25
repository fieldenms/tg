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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
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

    @IsProperty @MapTo @Required @Title(value = "Model", desc = "Model")
    private TgVehicleModel model;

    @IsProperty @MapTo @Title(value = "Price", desc = "Price")
    private Money price;

    @IsProperty @MapTo(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    public Money getPrice() {
	return price;
    }

    @Observable
    public TgVehicle setPrice(final Money price) {
	this.price = price;
	return this;
    }

    public Money getPurchasePrice() {
	return purchasePrice;
    }

    @Observable
    public TgVehicle setPurchasePrice(final Money purchasePrice) {
	this.purchasePrice = purchasePrice;
	return this;
    }

    public TgVehicleModel getModel() {
	return model;
    }

    @Observable  @EntityExists(TgVehicleModel.class)
    public TgVehicle setModel(final TgVehicleModel model) {
	this.model = model;
	return this;
    }

    public TgOrgUnit5 getStation() {
	return station;
    }

    @Observable  @EntityExists(TgOrgUnit5.class)
    public TgVehicle setStation(final TgOrgUnit5 station) {
	this.station = station;
	return this;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
    }
}
