package ua.com.fielden.platform.entity.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import javassist.util.proxy.ProxyFactory;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

public class EntityLazyLoadingTest extends AbstractDomainDrivenTestCase {

    private final ITgVehicle vehicleDao = getInstance(ITgVehicle.class);

    @Test
    public void lazily_loaded_vehicle_should_have_all_entity_props_proxied() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR1").getId());

        assertNull("Should be null", vehicle.getStation());
        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getModel().getClass()));
        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getReplacedBy().getClass()));
    }

    @Test
    public void accessing_lazily_loaded_property_model_should_initialise_it_with_lazy_proxy_properties() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR1").getId());

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getModel().getClass()));

        final TgVehicleMake make = vehicle.getModel().getMake();
        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(vehicle.getModel().getClass()));

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(make.getClass()));
    }

    @Test
    public void accessing_lazily_loaded_property_station_with_composite_key_should_be_initialised_with_non_proxied_key_members() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR2").getId());

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getClass()));

        final TgOrgUnit4 zone = vehicle.getStation().getParent();
        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getClass()));

        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(zone.getClass()));
        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(zone.getParent().getClass()));
    }

    @Test
    public void accessing_lazily_loaded_property_station_should_be_initialised_with_lazy_proxies_for_non_key_entity_props() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR2").getId());

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getClass()));

        final TgFuelType fuelType = vehicle.getStation().getFuelType();
        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getClass()));

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(fuelType.getClass()));
    }

    @Test
    public void deep_nested_access_to_lazily_loaded_property_should_work() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR2").getId());

        assertTrue("Should be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getClass()));

        final String fuelTypeCode = vehicle.getStation().getFuelType().getKey();
        assertEquals("Unexpected key value", "P", fuelTypeCode);
        assertFalse("Should not be proxy", ProxyFactory.isProxyClass(vehicle.getStation().getFuelType().getClass()));
    }


    @Test
    public void not_fetched_calculated_property_should_be_proxied_and_loaded_on_demand() {
        final TgVehicle vehicle = vehicleDao.lazyLoad(vehicleDao.findByKey("CAR2").getId());

        assertTrue(ProxyFactory.isProxyClass(vehicle.getLastFuelUsage().getClass()));
        assertEquals("P", vehicle.getLastFuelUsage().getFuelType().getKey());
    }

    @Override
    protected void populateDomain() {
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5").setFuelType(petrolFuelType));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
        final TgVehicleMake subaro = save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final TgVehicleModel m317 = save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        final TgVehicleModel m319 = save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        final TgVehicleModel m320 = save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        final TgVehicleModel m321 = save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        final TgVehicleModel m322 = save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5));
        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false).setReplacedBy(car2));

        save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}