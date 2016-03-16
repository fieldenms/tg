package ua.com.fielden.platform.entity.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class FetchModelTest extends BaseEntQueryTCase {

    private <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final fetch<T> fetchModel) {
        return EntityAggregates.class.equals(fetchModel.getEntityType()) ? new EntityAggregatesRetrievalModel<T>(fetchModel, DOMAIN_METADATA_ANALYSER) : //
                new EntityRetrievalModel<T>(fetchModel, DOMAIN_METADATA_ANALYSER);
    }

    @Test
    public void test_nested_fetching_of_composite_key() {
        final fetch<TgAuthorship> fetch = new fetch<TgAuthorship>(TgAuthorship.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("title"));
        assertTrue(fetchModel.containsProp("author"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertNotNull(fetchModel.getFetchModels().get("author"));
        final fetch<TgAuthor> exp = new fetch<TgAuthor>(TgAuthor.class, FetchCategory.MINIMAL);
        assertEquals("Should be equal", exp, fetchModel.getFetchModels().get("author"));
        final IRetrievalModel<TgAuthor> fetchModelForAuthor = produceRetrievalModel(exp);
        assertTrue(fetchModelForAuthor.containsProp("name"));
        assertTrue(fetchModelForAuthor.containsProp("surname"));
        assertTrue(fetchModelForAuthor.containsProp("id"));
        assertTrue(fetchModelForAuthor.containsProp("version"));
        //assertFalse(fetchModelForAuthor.containsProp("honorarium"));
        //assertFalse(fetchModelForAuthor.containsProp("honorarium.amount"));
        assertFalse(fetchModelForAuthor.containsProp("pseudonym"));
        assertNotNull(fetchModelForAuthor.getFetchModels().get("name"));
    }

    @Test
    public void test_all_fetching_of_make() {
        final fetch<TgVehicleMake> makeFetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.ALL);
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(makeFetch);
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_minimal_fetching_of_make() {
        final fetch<TgVehicleMake> fetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_id_and_version_fetching_of_make() {
        final fetch<TgVehicleMake> fetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.ID_AND_VERSTION);
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("key"));
        assertFalse(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_all_fetching_of_model() {
        final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.ALL);
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("make"));
    }

    @Test
    public void test_minimal_fetching_of_model() {
        final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("make"));
    }

    @Test
    public void test_id_and_version_fetching_of_model() {
        final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.ID_AND_VERSTION);
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("key"));
        assertFalse(fetchModel.containsProp("desc"));
        assertFalse(fetchModel.containsProp("make"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProxy("make"));
    }

    @Test
    public void test_all_fetching_of_fuel_usage() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.ALL);
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("vehicle"));
        assertTrue(fetchModel.containsProp("date"));
        assertTrue(fetchModel.containsProp("qty"));

    }

    @Test
    public void test_minimal_fetching_of_fuel_usage() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("vehicle"));
        assertTrue(fetchModel.containsProp("date"));
        assertTrue(fetchModel.containsProp("qty"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.ID_AND_VERSTION);
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("vehicle"));
        assertFalse(fetchModel.containsProp("date"));
        assertFalse(fetchModel.containsProp("qty"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.ID_AND_VERSTION).with("date").with("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("vehicle"));
        assertTrue(fetchModel.containsProp("date"));
        assertTrue(fetchModel.containsProp("qty"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage_without_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.MINIMAL).without("date").without("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("vehicle"));
        assertFalse(fetchModel.containsProp("date"));
        assertFalse(fetchModel.containsProp("qty"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_vehicle() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.ID_AND_VERSTION).with("vehicle");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("vehicle"));
        assertFalse(fetchModel.containsProp("date"));
        assertFalse(fetchModel.containsProp("qty"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        final fetch<? extends AbstractEntity<?>> vehicleFetchModel = fetchModel.getFetchModels().get("vehicle");
        assertTrue(vehicleFetchModel.getFetchCategory().equals(FetchCategory.MINIMAL));
        assertTrue(vehicleFetchModel.getEntityType().equals(TgVehicle.class));
        assertTrue(vehicleFetchModel.getIncludedPropsWithModels().size() == 0);
        assertTrue(vehicleFetchModel.getIncludedProps().size() == 0);
        assertTrue(vehicleFetchModel.getExcludedProps().size() == 0);
    }

    @Test
    public void test_all_fetching_of_bogie() {
        final fetch<TgBogie> bogieFetch = new fetch<TgBogie>(TgBogie.class, FetchCategory.ALL);
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(bogieFetch);
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("desc"));
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        final fetch<? extends AbstractEntity<?>> locationFetchModel = fetchModel.getFetchModels().get("location");
        assertTrue(locationFetchModel.getFetchCategory().equals(FetchCategory.ALL));
        assertTrue(locationFetchModel.getEntityType().equals(TgBogieLocation.class));
        assertTrue(locationFetchModel.getIncludedPropsWithModels().size() == 0);
        assertTrue(locationFetchModel.getIncludedProps().size() == 0);
        assertTrue(locationFetchModel.getExcludedProps().size() == 0);
    }

    @Test
    public void test_virtual_composite_key_property() {
        final fetch<TgAuthor> fetch = new fetch<TgAuthor>(TgAuthor.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertFalse(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("name"));
        assertTrue(fetchModel.containsProp("surname"));
    }

    @Test
    @Ignore
    public void test_entity_key_of_synchetic_entity() {
        final fetch<TgAverageFuelUsage> fetch = new fetch<TgAverageFuelUsage>(TgAverageFuelUsage.class, FetchCategory.MINIMAL);
        final IRetrievalModel<TgAverageFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("id"));
        assertFalse(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("qty"));
    }

    @Test
    public void fetch_only_works_with_union_entity_props() {
        final fetch<TgBogie> fetch1 = fetchOnly(TgBogie.class).with("id").with("key").with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final fetch<TgBogie> fetch2 = fetch(TgBogie.class).with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final IRetrievalModel<TgBogie> fetchModel1 = produceRetrievalModel(fetch1);
        final IRetrievalModel<TgBogie> fetchModel2 = produceRetrievalModel(fetch2);
        assertTrue(fetchModel1.containsProp("location"));
        System.out.println(fetchModel1);
        assertTrue(fetchModel2.containsProp("location"));
        System.out.println(fetchModel2);
    }

    @Test
    public void fetch_only_works() {
        final fetch<TgVehicle> fetch = fetchOnly(TgVehicle.class).with("key").with("station", fetchOnly(TgOrgUnit5.class));
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProp("version"));
        assertTrue(fetchModel.containsProp("key"));
        assertTrue(fetchModel.containsProp("station"));
        assertFalse(fetchModel.containsProp("desc"));
        assertFalse(fetchModel.containsProp("lastFuelUsage"));
        assertFalse(fetchModel.containsProp("constValueProp"));
    }

    @Test
    public void all_fetching_works() {
        final fetch<TgVehicle> fetch = fetchAll(TgVehicle.class).without("desc");
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertFalse(fetchModel.containsProp("lastFuelUsage"));
        assertFalse(fetchModel.containsProp("constValueProp"));
        assertTrue(fetchModel.containsProxy("lastFuelUsage"));
        assertTrue(fetchModel.containsProxy("desc"));
    }

    @Test
    public void all_calc_fetching_works() {
        final fetch<TgVehicle> fetch = fetchAllInclCalc(TgVehicle.class).without("key");
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("lastFuelUsage"));
        assertTrue(fetchModel.containsProp("constValueProp"));
    }

    @Test
    public void fetch_id_only_works() {
        final fetch<TgAuthorship> fetch = new fetch<TgAuthorship>(TgAuthorship.class, FetchCategory.ID);
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProxy("title"));
        assertTrue(fetchModel.containsProxy("author"));
        assertTrue(fetchModel.containsProxy("year"));
        assertTrue(fetchModel.containsProxy("version"));
        assertTrue(fetchModel.containsProp("id"));
    }

    @Test
    public void fetch_id_only_works_for_union_entity_props() {
        final fetch<TgBogie> fetch = new fetch<TgBogie>(TgBogie.class, FetchCategory.ID);
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(fetch);
        assertTrue(fetchModel.containsProp("id"));
        assertTrue(fetchModel.containsProxy("version"));
        assertTrue(fetchModel.containsProxy("key"));
        assertTrue(fetchModel.containsProxy("location"));
        assertFalse(fetchModel.containsProxy("location.workshop"));
    }
}