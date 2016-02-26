package ua.com.fielden.platform.entity.fetch;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
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
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

public class FetchModelReconstructionTest extends AbstractDomainDrivenTestCase {

    private final ITgVehicle vehicleDao = getInstance(ITgVehicle.class);

    @Test
    public void reconstruction_of_fetch_model_without_sub_models_should_succeed() {
        final fetch<TgVehicle> fetch = fetchOnly(TgVehicle.class).with("id").with("key").with("desc");
        final TgVehicle vehicle = vehicleDao.findByKeyAndFetch(fetch, "CAR1");

        final fetch<TgVehicle> reconFetch = FetchModelReconstructor.reconstruct(vehicle);

        assertSuperSet(fetch, reconFetch);
    }

    @Test
    public void reconstruction_of_fetch_model_with_sub_models_should_succeed() {
        final fetch<TgVehicle> fetch = fetch(TgVehicle.class).with("replacedBy", fetch(TgVehicle.class));
        final TgVehicle vehicle = vehicleDao.findByKeyAndFetch(fetch, "CAR1");

        final fetch<TgVehicle> reconFetch = FetchModelReconstructor.reconstruct(vehicle);

        assertSuperSet(fetch, reconFetch);
    }

    @Test
    public void reconstructed_fetch_model_should_not_contain_submodels_for_proxied_properties() {
        final TgVehicle vehicle = vehicleDao.findByKeyAndFetch(fetch(TgVehicle.class), "CAR1");

        final fetch<TgVehicle> reconFetch = FetchModelReconstructor.reconstruct(vehicle);

        assertFalse(reconFetch.getIncludedPropsWithModels().containsKey("replacedBy"));
        assertFalse(reconFetch.getIncudedProps().contains("replacedBy"));
    }

    @Test
    public void fetch_model_reconstruction_recognizes_instrumented_properties_to_produce_fetch_with_instrumentation() {
        final fetch<TgVehicle> fetch = fetch(TgVehicle.class).with("model", fetchAndInstrument(TgVehicleModel.class));
        final TgVehicle vehicle = vehicleDao.findByKeyAndFetch(fetch, "CAR1");

        final fetch<TgVehicle> reconFetch = FetchModelReconstructor.reconstruct(vehicle);
        assertSuperSet(fetch, reconFetch);
    }
    
    @Test
    public void fetch_model_reconstruction_recognizes_properties_of_type_AbstractUnionEntity() {
        final TgWorkshop workshop = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgBogieLocation location = config.getEntityFactory()
                .newEntity(TgBogieLocation.class)
                .setWorkshop(workshop);
        final TgBogie bogie = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
        assertEquals(workshop, bogie.getLocation().activeEntity());

        final fetch<TgBogie> expectedFetch = fetchAndInstrument(TgBogie.class).with("location", fetchAndInstrument(TgBogieLocation.class));
        final fetch<TgBogie> reconFetch = FetchModelReconstructor.reconstruct(bogie);
        
        assertSuperSet(expectedFetch, reconFetch);
    }

    public void assertSuperSet(final fetch<?> origModel, final fetch<?> superModel) {
        assertSuperSet(origModel, superModel, true);
    }

    private void assertSuperSet(final fetch<?> origModel, final fetch<?> superModel, boolean rootLevel) {
        assertTrue(format("Incomplete fetch model %s comparing to model %s.", superModel, origModel), superModel.getIncudedProps().containsAll(origModel.getIncudedProps())
                && (!rootLevel && superModel.isInstrumented() == origModel.isInstrumented() || rootLevel));

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> pair : origModel.getIncludedPropsWithModels().entrySet()) {
            assertSuperSet(pair.getValue(), superModel.getIncludedPropsWithModels().get(pair.getKey()), false);
        }
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
        save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").
                setInitDate(date("2007-01-01 00:00:00")).
                setModel(m316).
                setPrice(new Money("200")).
                setPurchasePrice(new Money("100")).
                setActive(false).
                setLeased(true).
                setLastMeterReading(new BigDecimal("105")).
                setStation(orgUnit5));
        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").
                setInitDate(date("2001-01-01 00:00:00")).
                setModel(m318).setPrice(new Money("20")).
                setPurchasePrice(new Money("10")).
                setActive(true).
                setLeased(false).
                setReplacedBy(car2));
        save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}