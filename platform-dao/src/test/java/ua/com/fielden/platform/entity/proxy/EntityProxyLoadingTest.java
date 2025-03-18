package ua.com.fielden.platform.entity.proxy;

import static javassist.util.proxy.ProxyFactory.isProxyClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyProxied;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;

public class EntityProxyLoadingTest extends AbstractDaoTestCase {

    private final ITgBogie coBogie = co(TgBogie.class);
    private final ITgVehicle coVehicle = co(TgVehicle.class);
    private final ITgWagon coWagon = co(TgWagon.class);

    private static void shouldNotBeProxy(Class<? extends AbstractEntity<?>> entityClass) {
        assertFalse("Should not be proxy", isProxyClass(entityClass));
    }

    private static void shouldBeProxy(final AbstractEntity<?> entity, final String propName) {
        assertTrue("Should be proxied: [%s.%s]".formatted(entity.getType().getSimpleName(), propName),
                   Reflector.isPropertyProxied(entity, propName));
    }

    private static void shouldNotBeProxy(final AbstractEntity<?> entity, final String propName) {
        assertFalse("Should not be proxy", Reflector.isPropertyProxied(entity, propName));
    }

    ///////////////////////////// primitive prop //////////////////////////////
    @Test
    public void not_null_primitive_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).without("desc")).model());
        shouldBeProxy(vehicle, "desc");
    }

    @Test
    public void not_null_calc_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle, "constValueProp");
    }
    
    ///////////////////////////// usual entity prop //////////////////////////////
    @Test
    public void not_null_entity_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        assertNotNull("Should be not null", vehicle.getReplacedBy().getId());
        shouldBeProxy(vehicle.getReplacedBy(), "model");
        shouldBeProxy(vehicle.getReplacedBy(), "key");
    }

    @Test
    public void null_entity_property_outside_fetch_model_should_be_also_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        assertNull("Should be null", vehicle.getReplacedBy());
    }

    @Test
    public void null_entity_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("replacedBy")).model());
        shouldNotBeProxy(vehicle, "replacedBy");
        assertNull(vehicle.getReplacedBy());
    }

    @Test
    public void not_null_entity_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("replacedBy")).model());
        shouldNotBeProxy(vehicle.getReplacedBy().getClass());
        shouldNotBeProxy(vehicle, "replacedBy");
        assertNotNull(vehicle.getReplacedBy());
    }

    ///////////////////////////// 1-2-1 entity prop (implicitly calculated prop) //////////////////////////////
    @Test
    public void not_null_121_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle, "finDetails");
    }

    @Test
    public void null_121_property_outside_fetch_model_should_also_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).model());
        shouldBeProxy(vehicle, "finDetails");
    }

    @Test
    public void not_null_121_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldNotBeProxy(vehicle, "finDetails");
        shouldNotBeProxy(vehicle.getFinDetails().getClass());
        assertNotNull(vehicle.getFinDetails());
    }

    @Test
    public void one_2_one_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldNotBeProxy(vehicle, "finDetails");
        assertNotNull(vehicle.getFinDetails());
    }
    
    ///////////////////////////// calculated entity prop (explicitly calculated prop) //////////////////////////////
    @Test
    public void not_null_calculated_property_outside_fetch_model_should_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("finDetails")).model());
        shouldBeProxy(vehicle, "lastFuelUsage");
    }
    
    @Test
    public void not_null_calculated_property_within_fetch_model_should_not_be_proxied() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("lastFuelUsage")).model());
        shouldNotBeProxy(vehicle, "lastFuelUsage");
        assertNotNull(vehicle.getLastFuelUsage().getId());
    }


    ///////////////////////////// other //////////////////////////////
    @Test
    public void test_fetch_all_with_calc_prop() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("lastFuelUsage")).model());
        shouldNotBeProxy(veh, "replacedBy");
        shouldNotBeProxy(veh, "lastFuelUsage");
        shouldNotBeProxy(veh.getLastFuelUsage(), "vehicle");
        shouldNotBeProxy(veh.getLastFuelUsage().getVehicle(), "replacedBy");
    }

    @Test
    public void test_fetch_all_with_121_prop() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("finDetails", fetch(TgVehicleFinDetails.class))).model());
        shouldNotBeProxy(veh, "replacedBy");
        shouldNotBeProxy(veh, "finDetails");
    }

    @Test
    public void test_fetch_of_one_to_one_master_entity_model() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle veh = coVehicle.getEntity(from(qry).with(fetchAll(TgVehicle.class).with("finDetails", fetch(TgVehicleFinDetails.class))).model());
        shouldNotBeProxy(veh, "model");
        shouldNotBeProxy(veh, "finDetails");
        shouldNotBeProxy(veh.getFinDetails(), "key");
        shouldNotBeProxy(veh.getFinDetails().getKey(), "model");
    }

    @Test
    public void test_query_with_union_property_being_null() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("key").eq().val("BOGIE2").model();
        TgBogie bogie = coBogie.getEntity(from(qry).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
        shouldNotBeProxy(bogie, "location");
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
        assertNull(vehicle1.getReplacedBy());
    }

    @Test
    public void properties_with_id_only_proxy_values_can_be_updated_with_null_and_saved() {
        final ITgVehicle coVehicle = co$(TgVehicle.class);
        final TgVehicle vehicle = coVehicle.findByKey("CAR2");

        assertFalse(Reflector.isPropertyProxied(vehicle, "station"));

        assertNotNull(vehicle.getStation());
        assertTrue(vehicle.getStation().isIdOnlyProxy());
        assertFalse(vehicle.getProperty("station").isDirty());

        vehicle.setStation(null);
        assertTrue(vehicle.getProperty("station").isDirty());
        assertNull(vehicle.getStation());

        final TgVehicle savedVehicle = save(vehicle);
        assertNull(savedVehicle.getStation());
    }

    @Test
    public void properties_with_id_only_proxy_values_cannot_be_updated_with_non_null_values() {
        final ITgVehicle coVehicle = co$(TgVehicle.class);
        final TgVehicle vehicle = coVehicle.findByKey("CAR2");

        assertFalse(Reflector.isPropertyProxied(vehicle, "station"));

        assertNotNull(vehicle.getStation());
        assertTrue(vehicle.getStation().isIdOnlyProxy());
        assertFalse(vehicle.getProperty("station").isDirty());

        final EntityResultQueryModel<TgOrgUnit5> query = select(TgOrgUnit5.class).where().prop("name").eq().val("orgunit5_1").model();
        final TgOrgUnit5 station51 = co(TgOrgUnit5.class).getEntity(from(query).with(fetchAll(TgOrgUnit5.class)).model());

        final Either<Exception, TgVehicle> setStationResult = Try(() -> vehicle.setStation(station51));
        assertTrue(setStationResult instanceof Left);
        final Left<Exception, TgVehicle> setError = (Left<Exception, TgVehicle>) setStationResult;
        assertTrue(setError.value() instanceof StrictProxyException);
    }

    @Test
    public void properties_that_could_have_id_only_proxy_values_but_are_null_can_be_updated_with_non_null_values() {
        final ITgVehicle coVehicle = co$(TgVehicle.class);
        final TgVehicle vehicle = coVehicle.findByKey("CAR1");

        assertFalse(Reflector.isPropertyProxied(vehicle, "station"));

        assertNull(vehicle.getStation());
        assertFalse(vehicle.getProperty("station").isDirty());

        final EntityResultQueryModel<TgOrgUnit5> query = select(TgOrgUnit5.class).where().prop("name").eq().val("orgunit5_1").model();
        final TgOrgUnit5 station51 = co(TgOrgUnit5.class).getEntity(from(query).with(fetchAll(TgOrgUnit5.class)).model());

        vehicle.setStation(station51);
        assertTrue(vehicle.getProperty("station").isDirty());
        assertEquals(station51, vehicle.getStation());

        final TgVehicle savedVehicle = save(vehicle);
        assertEquals(station51, savedVehicle.getStation());
    }

    @Test
    public void explicitly_unfetched_properties_of_entity_type_can_neither_be_accessed_nor_mutated() {
        final ITgVehicle coVehicle = co(TgVehicle.class);
        final fetch<TgVehicle> fetch = fetchKeyAndDescOnly(TgVehicle.class);
        final TgVehicle vehicle = coVehicle.findByKeyAndFetch(fetch, "CAR2");

        assertTrue(Reflector.isPropertyProxied(vehicle, "station"));

        final Either<Exception, TgOrgUnit5> getStationResult = Try(() -> vehicle.getStation());
        assertTrue(getStationResult instanceof Left);
        final Left<Exception, TgOrgUnit5> getError = (Left<Exception, TgOrgUnit5>) getStationResult;
        assertTrue(getError.value() instanceof StrictProxyException);

        final Either<Exception, TgVehicle> setStationResult = Try(() -> vehicle.setStation(null));
        assertTrue(setStationResult instanceof Left);
        final Left<Exception, TgVehicle> setError = (Left<Exception, TgVehicle>) setStationResult;
        assertTrue(setError.value() instanceof StrictProxyException);
    }
    
    @Test
    public void ordinary_non_persistent_properties_in_persistent_entities_do_not_get_proxied() {
        try (final Stream<TgVehicleModel> stream = co(TgVehicleModel.class).stream(from(select(TgVehicleModel.class).model()).with(fetchOnly(TgVehicleModel.class).with("key")).model())) {
            stream.forEach(vm -> {
                assertFalse("Oridinary, not persistent props should not be proxied", isPropertyProxied(vm, "ordinaryIntProp"));
                assertNull(vm.getOrdinaryIntProp());
                assertFalse("Fetched inherited @MapTo props should not be proxied.", isPropertyProxied(vm, "key"));
            });
        }
    }
    
    @Test
    public void properties_of_synthetic_entities_get_proxied_except_crit_only_ones() {
        try (final Stream<TgReVehicleModel> stream = co(TgReVehicleModel.class)
                                                    .stream(from(select(TgReVehicleModel.class).model())
                                                            .with(fetchOnly(TgReVehicleModel.class).with("key")).model())) {
            stream.forEach(vm -> {
                assertTrue("Not-fetched yielded props should be proxied", isPropertyProxied(vm, "intProp"));
                assertTrue("Not-feched and not-yielded props should be proxied", isPropertyProxied(vm, "noYieldIntProp"));
                assertTrue("Not-fetched inherited @MapTo props should be proxied", isPropertyProxied(vm, "make"));
                assertTrue("Even inherited, oridinary, not persistent props should become proxied in the context of a synthetic entity", isPropertyProxied(vm, "ordinaryIntProp"));
                assertTrue("Calculated properties should be proxied", isPropertyProxied(vm, "makeModelsCount"));
                assertFalse("@CritOnly props should not be proxied", isPropertyProxied(vm, "intCritProp"));
                assertFalse("Fetched inherited @MapTo props should not be proxied.", isPropertyProxied(vm, "key"));
                assertNull(vm.getIntCritProp());
            });
        }
    }

    @Test
    public void not_yielded_but_fetched_properties_of_synthetic_entities_do_not_get_proxied() {
        
        try (final Stream<TgReVehicleModel> stream = co(TgReVehicleModel.class)
                                                    .stream(from(select(TgReVehicleModel.class).model())
                                                            .with(fetchOnly(TgReVehicleModel.class).with("key").with("noYieldIntProp")).model())) {
            stream.forEach(vm -> {
                assertFalse("Feched and not-yielded props should not be proxied", isPropertyProxied(vm, "noYieldIntProp"));
                assertNull(vm.getNoYieldIntProp());
            });
        }
    }

    @Test
    public void non_fetched_collectional_properties_are_not_proxied_returning_an_empty_collection() {
        final EntityResultQueryModel<TgWagon> qry = select(TgWagon.class).where().prop("key").eq().val("WAGON1").model();
        final var wagonWithSlotsNotFetched = coWagon.getEntity(from(qry).with(fetch(TgWagon.class)).model());
        assertNotNull(wagonWithSlotsNotFetched);
        assertFalse(Reflector.isPropertyProxied(wagonWithSlotsNotFetched, "slots"));
        assertTrue(wagonWithSlotsNotFetched.getSlots().isEmpty());
        final var wagonWithSlotsFetched = coWagon.getEntity(from(qry).with(fetch(TgWagon.class).with("slots")).model());
        assertTrue(wagonWithSlotsFetched.getSlots().size() > 0);
    }

    @Test
    public void isPropertyProxied_sub_props_of_proxied_prop_are_proxied() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final var vehicle = coVehicle.getEntity(from(qry).with(fetch(TgVehicle.class)).model());
        assertFalse("model is not proxied", isPropertyProxied(vehicle, "model"));
        assertTrue("model.make is proxied", isPropertyProxied(vehicle, "model.make"));
        assertTrue("sub-properties of a proxied property are proxied", isPropertyProxied(vehicle, "model.make.key"));
    }

    @Test
    public void isPropertyProxied_sub_sub_sub_props_of_proxied_sub_sub_prop_are_proxied() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final var vehicle = coVehicle.getEntity(from(qry).with(fetch(TgVehicle.class).with("replacedBy", fetch(TgVehicle.class))).model());
        assertFalse("replacedBy is not proxied", isPropertyProxied(vehicle, "replacedBy"));
        assertNotNull(vehicle.getReplacedBy());
        assertFalse("replacedBy.model is not proxied", isPropertyProxied(vehicle, "replacedBy.model"));
        assertNotNull(vehicle.getReplacedBy().getModel());
        assertTrue("replacedBy.model.make is proxied", isPropertyProxied(vehicle, "replacedBy.model.make"));
        assertTrue("sub-sub-sub-properties of a proxied sub-sub-property are proxied", isPropertyProxied(vehicle, "replacedBy.model.make.key"));
    }

    @Test
    public void isPropertyProxied_sub_props_of_null_prop_are_not_proxied() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final var vehicle = coVehicle.getEntity(from(qry).with(fetch(TgVehicle.class).with("replacedBy", fetch(TgVehicle.class))).model());
        assertFalse(isPropertyProxied(vehicle, "replacedBy"));
        assertNull(vehicle.getReplacedBy());
        assertFalse("sub-properties of null are not proxied", isPropertyProxied(vehicle, "replacedBy.make"));
        assertFalse("sub-sub-properties of null are not proxied", isPropertyProxied(vehicle, "replacedBy.make.model"));
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        if (useSavedDataPopulationScript()) {
            return;
        }

        super.populateDomain();
        
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));

        final TgBogieLocation location = co$(TgBogieLocation.class).new_();
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
        final TgOrgUnit5 orgUnit5_1 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5_1"));

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

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

        final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
        final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
        final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
        final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
        final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
        final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

        final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true));
        final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1)); //.setPassword("password1")
        final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1));

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
    }

}
