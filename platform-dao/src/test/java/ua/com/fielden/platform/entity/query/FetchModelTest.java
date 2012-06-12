package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FetchModelTest extends BaseEntQueryTCase {

    @Test
    public void test_all_fetching_of_make() {
	final fetch<TgVehicleMake> makeFetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.ALL);
	final FetchModel<TgVehicleMake> fetchModel = new FetchModel<TgVehicleMake>(makeFetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("key"));
	assertTrue(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_minimal_fetching_of_make() {
	final fetch<TgVehicleMake> fetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.MINIMAL);
	final FetchModel<TgVehicleMake> fetchModel = new FetchModel<TgVehicleMake>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("key"));
	assertTrue(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_none_fetching_of_make() {
	final fetch<TgVehicleMake> fetch = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.NONE);
	final FetchModel<TgVehicleMake> fetchModel = new FetchModel<TgVehicleMake>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertFalse(fetchModel.containsProp("key"));
	assertFalse(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_all_fetching_of_model() {
	final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.ALL);
	final FetchModel<TgVehicleModel> fetchModel = new FetchModel<TgVehicleModel>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("key"));
	assertTrue(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	assertTrue(fetchModel.containsProp("make"));
    }

    @Test
    public void test_minimal_fetching_of_model() {
	final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.MINIMAL);
	final FetchModel<TgVehicleModel> fetchModel = new FetchModel<TgVehicleModel>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("key"));
	assertTrue(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	assertFalse(fetchModel.containsProp("make"));
    }

    @Test
    public void test_none_fetching_of_model() {
	final fetch<TgVehicleModel> fetch = new fetch<TgVehicleModel>(TgVehicleModel.class, FetchCategory.NONE);
	final FetchModel<TgVehicleModel> fetchModel = new FetchModel<TgVehicleModel>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertFalse(fetchModel.containsProp("key"));
	assertFalse(fetchModel.containsProp("desc"));
	assertFalse(fetchModel.containsProp("make"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_all_fetching_of_fuel_usage() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.ALL);
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	assertTrue(fetchModel.containsProp("vehicle"));
	assertTrue(fetchModel.containsProp("date"));
	assertTrue(fetchModel.containsProp("qty"));

    }

    @Test
    public void test_minimal_fetching_of_fuel_usage() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.MINIMAL);
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	assertTrue(fetchModel.containsProp("vehicle"));
	assertTrue(fetchModel.containsProp("date"));
	assertTrue(fetchModel.containsProp("qty"));
    }

    @Test
    public void test_none_fetching_of_fuel_usage() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.NONE);
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertFalse(fetchModel.containsProp("vehicle"));
	assertFalse(fetchModel.containsProp("date"));
	assertFalse(fetchModel.containsProp("qty"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_none_fetching_of_fuel_usage_with_date_and_qty() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.NONE).with("date").with("qty");
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertFalse(fetchModel.containsProp("vehicle"));
	assertTrue(fetchModel.containsProp("date"));
	assertTrue(fetchModel.containsProp("qty"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage_without_date_and_qty() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.MINIMAL).without("date").without("qty");
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	assertTrue(fetchModel.containsProp("vehicle"));
	assertFalse(fetchModel.containsProp("date"));
	assertFalse(fetchModel.containsProp("qty"));
    }

    @Test
    public void test_none_fetching_of_fuel_usage_with_vehicle() {
	final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, FetchCategory.NONE).with("vehicle");
	final FetchModel<TgFuelUsage> fetchModel = new FetchModel<TgFuelUsage>(fetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("vehicle"));
	assertFalse(fetchModel.containsProp("date"));
	assertFalse(fetchModel.containsProp("qty"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	final fetch<? extends AbstractEntity<?>> vehicleFetchModel = fetchModel.getFetchModels().get("vehicle");
	assertTrue(vehicleFetchModel.getFetchCategory().equals(FetchCategory.MINIMAL));
	assertTrue(vehicleFetchModel.getEntityType().equals(TgVehicle.class));
	assertTrue(vehicleFetchModel.getIncludedPropsWithModels().size() == 0);
	assertTrue(vehicleFetchModel.getIncudedProps().size() == 0);
	assertTrue(vehicleFetchModel.getExcludedProps().size() == 0);
    }

    @Test
    public void test_all_fetching_of_bogie() {
	final fetch<TgBogie> bogieFetch = new fetch<TgBogie>(TgBogie.class, FetchCategory.ALL);
	final FetchModel<TgBogie> fetchModel = new FetchModel<TgBogie>(bogieFetch, DOMAIN_PERSISTENCE_METADATA_ANALYSER);
	assertTrue(fetchModel.containsProp("key"));
	assertTrue(fetchModel.containsProp("desc"));
	assertTrue(fetchModel.containsProp("id"));
	assertTrue(fetchModel.containsProp("version"));
	final fetch<? extends AbstractEntity<?>> locationFetchModel = fetchModel.getFetchModels().get("location");
	assertTrue(locationFetchModel.getFetchCategory().equals(FetchCategory.ALL));
	assertTrue(locationFetchModel.getEntityType().equals(TgBogieLocation.class));
	assertTrue(locationFetchModel.getIncludedPropsWithModels().size() == 0);
	assertTrue(locationFetchModel.getIncudedProps().size() == 0);
	assertTrue(locationFetchModel.getExcludedProps().size() == 0);
    }
}