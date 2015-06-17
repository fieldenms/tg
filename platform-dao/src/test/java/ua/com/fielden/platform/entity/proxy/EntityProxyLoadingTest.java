package ua.com.fielden.platform.entity.proxy;

import static javassist.util.proxy.ProxyFactory.isProxyClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

public class EntityProxyLoadingTest extends AbstractDomainDrivenTestCase {

    private final ITgBogie coBogie = getInstance(ITgBogie.class);
    private final ITgVehicle coVehicle = getInstance(ITgVehicle.class);

    private static void shouldBeProxy(Class<? extends AbstractEntity<?>> entityClass) {
        assertTrue("Should be proxy", isProxyClass(entityClass));
    }

    private static void shouldNotBeProxy(Class<? extends AbstractEntity<?>> entityClass) {
        assertFalse("Should not be proxy", isProxyClass(entityClass));
    }

    private static void shouldBeProxy(MetaProperty<?> metaProperty) {
        assertTrue("Should be proxy", metaProperty.isProxy());
    }

    private static void shouldNotBeProxy(MetaProperty<?> metaProperty) {
        assertFalse("Should not be proxy", metaProperty.isProxy());
    }

    ///////////////////////////// usual entity prop //////////////////////////////
    @Test
    public void not_null_entity_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle.getReplacedBy().getClass());
        shouldBeProxy(vehicle.getProperty("replacedBy"));
    }

    @Test
    public void null_entity_property_outside_fetch_model_should_be_also_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle.getProperty("replacedBy"));
    }

    @Test
    public void null_entity_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("replacedBy")).model());
        shouldNotBeProxy(vehicle.getProperty("replacedBy"));
        assertNull(vehicle.getReplacedBy());
    }

    @Test
    public void not_null_entity_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("replacedBy")).model());
        shouldNotBeProxy(vehicle.getReplacedBy().getClass());
        shouldNotBeProxy(vehicle.getProperty("replacedBy"));
        assertNotNull(vehicle.getReplacedBy());
    }

    ///////////////////////////// 1-2-1 entity prop (implicitly calculated prop) //////////////////////////////
    @Test
    public void not_null_121_property_outside_fetch_model_should_be_strictly_proxied_but_not_loaded() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle.getProperty("finDetails"));
        assertNull(vehicle.getFinDetails().getId());
    }

    @Test
    public void null_121_property_outside_fetch_model_should_also_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle.getProperty("finDetails"));
    }

    @Test
    public void not_null_121_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldNotBeProxy(vehicle.getProperty("finDetails"));
        shouldNotBeProxy(vehicle.getFinDetails().getClass());
        assertNotNull(vehicle.getFinDetails());
    }

    @Test
    public void null_121_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldNotBeProxy(vehicle.getProperty("finDetails"));
        assertNull(vehicle.getFinDetails());
    }
    
    ///////////////////////////// calculated entity prop (explicitly calculated prop) //////////////////////////////
    @Test
    public void not_null_calculated_property_outside_fetch_model_should_be_strictly_proxied_but_not_loaded() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldBeProxy(vehicle.getProperty("lastFuelUsage"));
        assertNull(vehicle.getLastFuelUsage().getId());
    }
    
    @Test
    public void not_null_calculated_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("lastFuelUsage")).model());
        shouldNotBeProxy(vehicle.getProperty("lastFuelUsage"));
        assertNotNull(vehicle.getLastFuelUsage().getId());
    }


    ///////////////////////////// other //////////////////////////////
    @Test
    public void test_fetch_all_with_calc_prop() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("lastFuelUsage")).model());
        shouldNotBeProxy(veh.getProperty("replacedBy"));
        shouldNotBeProxy(veh.getProperty("lastFuelUsage"));
        shouldNotBeProxy(veh.getLastFuelUsage().getProperty("vehicle"));
        shouldNotBeProxy(veh.getLastFuelUsage().getVehicle().getProperty("replacedBy"));
    }

    @Test
    public void test_fetch_all_with_121_prop() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("finDetails", fetch(TgVehicleFinDetails.class))).model());
        shouldNotBeProxy(veh.getProperty("replacedBy"));
        shouldNotBeProxy(veh.getProperty("finDetails"));
    }

    @Test
    public void test_fetch_of_one_to_one_master_entity_model() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("finDetails", fetch(TgVehicleFinDetails.class))).model());
        shouldNotBeProxy(veh.getProperty("model"));
        shouldNotBeProxy(veh.getProperty("finDetails"));
        shouldNotBeProxy(veh.getFinDetails().getProperty("key"));
        shouldNotBeProxy(veh.getFinDetails().getKey().getProperty("model"));
    }

    @Test
    public void test_query_with_union_property_being_null() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("key").eq().val("BOGIE2").model();
        TgBogie bogie = coBogie.getEntity(from(qry).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
        shouldNotBeProxy(bogie.getProperty("location"));
        assertNull(bogie.getLocation());
    }
    
    @Test
    public void test_query_for_correct_fetching_adjustment() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).
                where().prop("key").eq().val("CAR2").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("model").as("model").
                yield().prop("model.make").as("model.make").
                yield().prop("model.make.key").as("model.make.key").
                modelAsEntity(TgVehicle.class);
        final TgVehicle vehicle1 = coVehicle.getEntity(from(qry).with(fetch(TgVehicle.class).
                with("key").
                with("desc").
                with("model", fetchOnly(TgVehicleModel.class).
                        with("key").
                        with("make", fetchOnly(TgVehicleMake.class).
                                with("key")))).model());
        assertNotNull(vehicle1.getModel().getMake().getKey());
        assertTrue(vehicle1.getProperty("replacedBy").isProxy());
        assertNull(vehicle1.getReplacedBy().getId());
    }

    @Override
    protected void populateDomain() {
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));

        final TgBogieLocation location = config.getEntityFactory().newEntity(TgBogieLocation.class);
        location.setWorkshop(workshop1);
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2"));
        final TgBogie bogie3 = save(new_(TgBogie.class, "BOGIE3", "Bogie 3"));
        final TgBogie bogie4 = save(new_(TgBogie.class, "BOGIE4", "Bogie 4"));
        final TgBogie bogie5 = save(new_(TgBogie.class, "BOGIE5", "Bogie 5"));
        final TgBogie bogie6 = save(new_(TgBogie.class, "BOGIE6", "Bogie 6"));
        final TgBogie bogie7 = save(new_(TgBogie.class, "BOGIE7", "Bogie 7"));

        final TgWagon wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final TgWagon wagon2 = save(new_(TgWagon.class, "WAGON2", "Wagon 2"));

        save(new_composite(TgWagonSlot.class, wagon1, 5));
        save(new_composite(TgWagonSlot.class, wagon1, 6));
        save(new_composite(TgWagonSlot.class, wagon1, 7));
        save(new_composite(TgWagonSlot.class, wagon1, 8));
        save(new_composite(TgWagonSlot.class, wagon1, 4).setBogie(bogie1));
        save(new_composite(TgWagonSlot.class, wagon1, 3).setBogie(bogie2));
        save(new_composite(TgWagonSlot.class, wagon1, 2).setBogie(bogie3));
        save(new_composite(TgWagonSlot.class, wagon1, 1).setBogie(bogie4));

        save(new_composite(TgWagonSlot.class, wagon2, 1).setBogie(bogie5));
        save(new_composite(TgWagonSlot.class, wagon2, 2).setBogie(bogie6));
        save(new_composite(TgWagonSlot.class, wagon2, 3).setBogie(bogie7));

        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5"));

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

        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5).setReplacedBy(car1));

        save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

        final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
        final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
        final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
        final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
        final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
        final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

        final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true).setPassword("password1"));
        final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));
        final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));
        final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));

        save(new_composite(UserAndRoleAssociation.class, user1, managerRole));
        save(new_composite(UserAndRoleAssociation.class, user1, analyticRole));
        save(new_composite(UserAndRoleAssociation.class, user2, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user2, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user2, warehouseOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user3, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, warehouseOperatorRole));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        save(new_composite(TgAuthor.class, chris, "Date", null));

        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych"));

        System.out.println("\n\n\n\n\n\n\n\n\n   =====  DATA POPULATED SUCCESSFULLY   =====\n\n\n\n\n\n\n\n\n");
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}