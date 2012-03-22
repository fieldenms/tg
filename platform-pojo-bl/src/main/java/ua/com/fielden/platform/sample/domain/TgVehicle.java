package ua.com.fielden.platform.sample.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@DefaultController2(ITgVehicle.class)
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

    @IsProperty @MapTo @Title(value = "Active", desc = "Active")
    private boolean active;

    @IsProperty @MapTo @Title(value = "Leased", desc = "Leased?")
    private boolean leased;

    @IsProperty(TgFuelUsage.class)  @MapTo("VEHICLE_") @Title(value = "Fuel usages", desc = "Fuel usages")
    private Set<TgFuelUsage> fuelUsages = new HashSet<TgFuelUsage>();
    public Set<TgFuelUsage> getFuelUsages() { return fuelUsages; }

    @Observable
    public void setFuelUsages(final Set<TgFuelUsage> fuelUsages) {
	this.fuelUsages = fuelUsages;
    }

    @Observable
    public TgVehicle setLeased(final boolean leased) {
	this.leased = leased;
	return this;
    }

    public boolean getLeased() {
	return leased;
    }

    @Observable
    public TgVehicle setActive(final boolean active) {
	this.active = active;
	return this;
    }

    public boolean getActive() {
	return active;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
    }

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

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public void setInitDate(final Date initDate) {
        this.initDate = initDate;
    }

    public TgVehicle getReplacedBy() {
        return replacedBy;
    }

    @Observable  @EntityExists(TgVehicle.class)
    public void setReplacedBy(final TgVehicle replacedBy) {
        this.replacedBy = replacedBy;
    }
}