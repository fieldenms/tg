package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ALL;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ALL_INCL_CALC;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.DEFAULT;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_AND_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_ONLY;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.KEY_AND_DESC;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.Set;

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
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class FetchModelTest extends BaseEntQueryTCase {

    private static <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final fetch<T> fetchModel) {
        return EntityAggregates.class.equals(fetchModel.getEntityType()) ? new EntityAggregatesRetrievalModel<T>(fetchModel, DOMAIN_METADATA_ANALYSER) : //
                new EntityRetrievalModel<T>(fetchModel, DOMAIN_METADATA_ANALYSER);
    }
    
    private static <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final Class<T> entityType, final FetchCategory fetchCategory) {
        return produceRetrievalModel(new fetch<T>(entityType, fetchCategory));
    }
    
    private static <T extends AbstractEntity<?>> void assertPropsAreFetched(final IRetrievalModel<T> fetchModel, final Set<String> props) {
        for (final String propName : props) {
            assertTrue(format("Property [%s] should be contained within fetch model:\n%s", propName, fetchModel), fetchModel.containsProp(propName));
        }
    }
    
    private static <T extends AbstractEntity<?>> void assertPropsAreProxied(final IRetrievalModel<T> fetchModel, final Set<String> proxiedProps) {
        for (final String propName : proxiedProps) {
            assertTrue(format("Property [%s] should be proxied within fetch model:\n%s", propName, fetchModel), fetchModel.containsProxy(propName));
        }
    }

    @Test
    public void test_nested_fetching_of_composite_key() {
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(TgAuthorship.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "title", "author", "year"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author"), setOf("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author").getRetrievalModels().get("name"), setOf("id", "version", "key", "desc"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("pseudonym"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("honorarium"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("honorarium.amount"));
    }
    
    @Test
    public void test_all_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, ALL);
        assertPropsAreFetched(fetchModel, setOf("key", "desc", "id", "version"));
    }

    @Test
    public void test_minimal_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("key", "desc", "id", "version"));
    }

    @Test
    public void test_id_and_version_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, setOf("id", "version"));
        assertPropsAreProxied(fetchModel, setOf("key", "desc"));
    }

    @Test
    public void test_all_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, ALL);
        assertPropsAreFetched(fetchModel, setOf("key", "desc", "id", "version", "make"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("make"), setOf("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("key", "desc", "id", "version", "make"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("make"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("make"), setOf("version", "key", "desc"));
    }

    @Test
    public void test_id_and_version_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, setOf("id", "version"));
        assertPropsAreProxied(fetchModel, setOf("key", "desc", "make"));
    }

    @Test
    public void test_all_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, ALL);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "vehicle", "date", "qty", "fuelType"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), setOf("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), setOf("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "vehicle", "date", "qty"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), setOf("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("version", "key", "desc"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("fuelType"), setOf("version", "key", "desc"));
    }

    @Test
    public void test_key_and_desc_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, KEY_AND_DESC);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "vehicle", "date"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), setOf("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("version", "key", "desc"));
       
        assertPropsAreProxied(fetchModel, setOf("qty", "fuelType"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, setOf("id", "version"));
        assertPropsAreProxied(fetchModel, setOf("vehicle", "date", "fuelType", "qty"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, ID_AND_VERSION).with("date").with("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "date", "qty"));
        assertPropsAreProxied(fetchModel, setOf("vehicle", "fuelType"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage_without_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, DEFAULT).without("date").without("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "vehicle", "fuelType"));
        assertPropsAreProxied(fetchModel, setOf("date", "qty"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), setOf("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("fuelType"), setOf("version", "key", "desc"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_vehicle() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, ID_AND_VERSION).with("vehicle");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "vehicle"));
        assertPropsAreProxied(fetchModel, setOf("qty", "date", "fuelType"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), setOf("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), setOf("version", "key", "desc"));
    }

    @Test
    public void test_all_fetching_of_bogie() {
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(TgBogie.class, ALL);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "key", "desc", "location"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location"), setOf("workshop", "wagonSlot"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("workshop"), setOf("id", "version", "key", "desc"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot"), setOf("id", "version", "wagon", "position"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot").getRetrievalModels().get("wagon"), setOf("id", "version", "key", "desc", "serialNo", "wagonClass"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot").getRetrievalModels().get("wagon").getRetrievalModels().get("wagonClass"), setOf("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot").getRetrievalModels().get("wagon").getRetrievalModels().get("wagonClass"), setOf("version", "key", "desc", "numberOfBogies", "numberOfWheelsets", "tonnage"));
    }

    @Test
    public void test_virtual_composite_key_property() {
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(TgAuthor.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("name"), setOf("id", "version", "key", "desc"));
        assertFalse(fetchModel.containsProp("pseudonym"));
        assertFalse(fetchModel.containsProp("honorarium"));
        assertFalse(fetchModel.containsProp("honorarium.amount"));

    }

    @Test
    @Ignore
    public void test_entity_key_of_synchetic_entity() {
        final IRetrievalModel<TgAverageFuelUsage> fetchModel = produceRetrievalModel(TgAverageFuelUsage.class, DEFAULT);
        assertPropsAreFetched(fetchModel, setOf("qty", "key"));
        assertFalse(fetchModel.containsProp("id"));
        assertFalse(fetchModel.containsProp("version"));
    }

    @Test
    public void fetch_only_works_with_union_entity_props() {
        final fetch<TgBogie> fetch1 = fetchOnly(TgBogie.class).with("id").with("key").with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final fetch<TgBogie> fetch2 = fetch(TgBogie.class).with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final IRetrievalModel<TgBogie> fetchModel1 = produceRetrievalModel(fetch1);
        final IRetrievalModel<TgBogie> fetchModel2 = produceRetrievalModel(fetch2);
        assertTrue(fetchModel1.containsProp("location"));
        //System.out.println(fetchModel1);
        assertTrue(fetchModel2.containsProp("location"));
        //System.out.println(fetchModel2);
    }

    @Test
    public void fetch_only_works() {
        final fetch<TgVehicle> fetch = fetchOnly(TgVehicle.class).with("key").with("station", fetchOnly(TgOrgUnit5.class));
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "key", "station"));
        assertPropsAreProxied(fetchModel, setOf("desc", "initDate", "replacedBy", "model", "price", "purchasePrice", "active", "leased", "lastMeterReading"));
        assertFalse(fetchModel.containsProp("lastFuelUsage"));
        assertFalse(fetchModel.containsProp("constValueProp"));
        assertFalse(fetchModel.containsProp("lastFuelUsageQty"));
        assertFalse(fetchModel.containsProp("sumOfPrices"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("station"), setOf("id", "version"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("station"), setOf("parent", "name", "fuelType"));

    }

    @Test
    public void all_fetching_works() {
        final fetch<TgVehicle> fetch = fetchAll(TgVehicle.class).without("desc");
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "key", "station", "initDate", "replacedBy", "model", "price", "purchasePrice", "active", "leased", "lastMeterReading"));
        assertPropsAreProxied(fetchModel, setOf("desc"));
        assertFalse(fetchModel.containsProp("lastFuelUsage"));
        assertFalse(fetchModel.containsProp("constValueProp"));
        assertFalse(fetchModel.containsProp("lastFuelUsageQty"));
        //assertFalse(fetchModel.containsProp("sumOfPrices")); //TOFIX
    }

    @Test
    public void all_calc_fetching_works() {
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(TgAuthor.class, ALL_INCL_CALC);
        assertPropsAreFetched(fetchModel, setOf("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage", "lastRoyalty", "hasMultiplePublications"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("name"), setOf("id", "version", "key", "desc"));
        assertFalse(fetchModel.containsProp("pseudonym"));
        assertFalse(fetchModel.containsProp("honorarium"));
        assertFalse(fetchModel.containsProp("honorarium.amount"));    }

    
    @Test
    public void fetch_id_only_works() {
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(TgAuthorship.class, ID_ONLY);
        assertPropsAreFetched(fetchModel, setOf("id"));
        assertPropsAreProxied(fetchModel, setOf("version", "title", "author", "year"));
    }

    @Test
    public void fetch_id_only_works_for_union_entity_props() {
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(TgBogie.class, ID_ONLY);
        assertPropsAreFetched(fetchModel, setOf("id"));
        assertPropsAreProxied(fetchModel, setOf("version", "key", "location"));
    }
    
    @Test
    public void fetch_composite_key_of_synthetic_entity() {
        final IRetrievalModel<TgPublishedYearly> fetchModel = produceRetrievalModel(TgPublishedYearly.class, KEY_AND_DESC);
        assertPropsAreFetched(fetchModel, setOf("author"));
        assertPropsAreProxied(fetchModel, setOf("qty"));

        assertFalse(fetchModel.containsProp("id"));
        assertFalse(fetchModel.containsProxy("id"));
        assertFalse(fetchModel.containsProp("version"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author"), setOf("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author").getRetrievalModels().get("name"), setOf("id", "version", "key", "desc"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("pseudonym"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("honorarium"));
        assertFalse(fetchModel.getRetrievalModels().get("author").containsProp("honorarium.amount"));

    }
}