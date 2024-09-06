package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.test_entities.Circular_EntityWithCompositeKeyMemberUnionEntity;
import ua.com.fielden.platform.entity.query.test_entities.Circular_UnionEntity;
import ua.com.fielden.platform.eql.meta.BaseEntQueryTCase1;
import ua.com.fielden.platform.sample.domain.*;

import java.util.Set;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.*;

public class FetchModelTest extends BaseEntQueryTCase1 {

    private static <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final fetch<T> fetchModel) {
        return EntityAggregates.class.equals(fetchModel.getEntityType()) ? new EntityAggregatesRetrievalModel<T>(fetchModel, DOMAIN_METADATA) : //
                new EntityRetrievalModel<T>(fetchModel, DOMAIN_METADATA);
    }
    
    private static <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final Class<T> entityType, final FetchCategory fetchCategory) {
        return produceRetrievalModel(new fetch<T>(entityType, fetchCategory));
    }
    
    private static <T extends AbstractEntity<?>> void assertPropsAreFetched(final IRetrievalModel<T> fetchModel, final Set<String> props) {
        for (final String propName : props) {
            assertTrue(format("Property [%s] should be contained within fetch model:\n%s", propName, fetchModel), fetchModel.containsProp(propName));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreNotFetched(final IRetrievalModel<T> fetchModel, final Set<String> props) {
        for (final String prop : props) {
            assertFalse(format("Property [%s] should not be contained within fetch model:\n%s", prop, fetchModel),
                        fetchModel.containsProp(prop));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreProxied(final IRetrievalModel<T> fetchModel, final Set<String> proxiedProps) {
        for (final String propName : proxiedProps) {
            assertTrue(format("Property [%s] should be proxied within fetch model:\n%s", propName, fetchModel), fetchModel.containsProxy(propName));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreNotProxied(final IRetrievalModel<T> fetchModel, final Set<String> props) {
        for (final String prop : props) {
            assertFalse(format("Property [%s] should not be proxied within fetch model:\n%s", prop, fetchModel),
                        fetchModel.containsProxy(prop));
        }
    }

    @Test
    public void test_nested_fetching_of_composite_key() {
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(TgAuthorship.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "title", "author", "year"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author"),
                              Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author").getRetrievalModels().get("name"),
                              Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel.getRetrievalModels().get("author"), Set.of("pseudonym"));
        assertPropsAreNotFetched(fetchModel.getRetrievalModels().get("author"), Set.of("honorarium"));
        assertPropsAreNotFetched(fetchModel.getRetrievalModels().get("author"), Set.of("honorarium.amount"));
    }
    
    @Test
    public void test_all_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, ALL);
        assertPropsAreFetched(fetchModel, Set.of("key", "desc", "id", "version"));
    }

    @Test
    public void test_minimal_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("key", "desc", "id", "version"));
    }

    @Test
    public void test_id_and_version_fetching_of_make() {
        final IRetrievalModel<TgVehicleMake> fetchModel = produceRetrievalModel(TgVehicleMake.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, Set.of("id", "version"));
        assertPropsAreProxied(fetchModel, Set.of("key", "desc"));
    }

    @Test
    public void test_all_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, ALL);
        assertPropsAreFetched(fetchModel, Set.of("key", "desc", "id", "version", "make"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("make"), Set.of("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("key", "desc", "id", "version", "make"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("make"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("make"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_id_and_version_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, Set.of("id", "version"));
        assertPropsAreProxied(fetchModel, Set.of("key", "desc", "make"));
    }

    @Test
    public void test_all_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, ALL);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "date", "qty", "fuelType"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), Set.of("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "date", "qty"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("version", "key", "desc"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("fuelType"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_key_and_desc_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, KEY_AND_DESC);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "date"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("version", "key", "desc"));
       
        assertPropsAreProxied(fetchModel, Set.of("qty", "fuelType"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, ID_AND_VERSION);
        assertPropsAreFetched(fetchModel, Set.of("id", "version"));
        assertPropsAreProxied(fetchModel, Set.of("vehicle", "date", "fuelType", "qty"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, ID_AND_VERSION).with("date").with("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "date", "qty"));
        assertPropsAreProxied(fetchModel, Set.of("vehicle", "fuelType"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage_without_date_and_qty() {
        final fetch<TgFuelUsage> fetch = new fetch<TgFuelUsage>(TgFuelUsage.class, DEFAULT).without("date").without("qty");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "fuelType"));
        assertPropsAreProxied(fetchModel, Set.of("date", "qty"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("fuelType"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("fuelType"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_vehicle() {
        final fetch<TgFuelUsage> fetch = new fetch<>(TgFuelUsage.class, ID_AND_VERSION).with("vehicle");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle"));
        assertPropsAreProxied(fetchModel, Set.of("qty", "date", "fuelType"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("vehicle").getRetrievalModels().get("model"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_all_fetching_of_bogie() {
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(TgBogie.class, ALL);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "key", "desc", "location"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location"), Set.of("workshop", "wagonSlot"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("workshop"),
                              Set.of("id", "version", "key", "desc"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot"),
                              Set.of("id", "version", "wagon", "position"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot")
                                      .getRetrievalModels().get("wagon"),
                              Set.of("id", "version", "key", "desc", "serialNo", "wagonClass"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot")
                                      .getRetrievalModels().get("wagon").getRetrievalModels().get("wagonClass"),
                              Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("location").getRetrievalModels().get("wagonSlot")
                                      .getRetrievalModels().get("wagon").getRetrievalModels().get("wagonClass"),
                              Set.of("version", "key", "desc", "numberOfBogies", "numberOfWheelsets", "tonnage"));
    }

    @Test
    public void test_virtual_composite_key_property() {
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(TgAuthor.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("name"), Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel, Set.of("pseudonym", "honorarium", "honorarium.amount"));
    }

    @Test
    public void strategy_DEFAULT_for_synthetic_entity() {
        final IRetrievalModel<TgAverageFuelUsage> fetchModel = produceRetrievalModel(TgAverageFuelUsage.class, DEFAULT);
        // FIXME: datePeriod is not yielded and therefore should not be included into the fetch model
        assertPropsAreFetched(fetchModel, Set.of("key", "qty", "cost", "datePeriod"));
        assertPropsAreNotFetched(fetchModel, Set.of("id", "version"));
    }

    @Test
    public void fetch_only_works_with_union_entity_props() {
        final fetch<TgBogie> fetch1 = fetchOnly(TgBogie.class).with("id").with("key").with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final fetch<TgBogie> fetch2 = fetch(TgBogie.class).with("location", fetchOnly(TgBogieLocation.class).with("workshop"));
        final IRetrievalModel<TgBogie> fetchModel1 = produceRetrievalModel(fetch1);
        final IRetrievalModel<TgBogie> fetchModel2 = produceRetrievalModel(fetch2);
        assertPropsAreFetched(fetchModel1, Set.of("location"));
        assertPropsAreFetched(fetchModel2, Set.of("location"));
    }

    @Test
    public void fetch_only_works() {
        final fetch<TgVehicle> fetch = fetchOnly(TgVehicle.class).with("key").with("station", fetchOnly(TgOrgUnit5.class));
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "key", "station"));
        assertPropsAreProxied(fetchModel, Set.of("desc", "initDate", "replacedBy", "model", "price", "purchasePrice",
                                                 "active", "leased", "lastMeterReading"));
        assertPropsAreNotFetched(fetchModel, Set.of("lastFuelUsage", "constValueProp", "lastFuelUsageQty", "sumOfPrices"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("station"), Set.of("id", "version"));
        assertPropsAreProxied(fetchModel.getRetrievalModels().get("station"), Set.of("parent", "name", "fuelType"));

    }

    @Test
    public void all_fetching_works() {
        final fetch<TgVehicle> fetch = fetchAll(TgVehicle.class).without("desc");
        final IRetrievalModel<TgVehicle> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "key", "station", "initDate", "replacedBy", "model",
                                                 "price", "purchasePrice", "active", "leased", "lastMeterReading"));
        assertPropsAreProxied(fetchModel, Set.of("desc"));
        assertPropsAreNotFetched(fetchModel, Set.of("lastFuelUsage", "constValueProp", "lastFuelUsageQty"));
        //assertFalse(fetchModel.containsProp("sumOfPrices")); //TOFIX
    }

    @Test
    public void all_calc_fetching_works() {
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(TgAuthor.class, ALL_INCL_CALC);
        assertPropsAreFetched(fetchModel,
                              Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage",
                                    "lastRoyalty", "hasMultiplePublications"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("name"), Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel, Set.of("pseudonym", "honorarium", "honorarium.amount"));
    }

    
    @Test
    public void fetch_id_only_works() {
        final IRetrievalModel<TgAuthorship> fetchModel = produceRetrievalModel(TgAuthorship.class, ID_ONLY);
        assertPropsAreFetched(fetchModel, Set.of("id"));
        assertPropsAreProxied(fetchModel, Set.of("version", "title", "author", "year"));
    }

    @Test
    public void fetch_id_only_works_for_union_entity_props() {
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(TgBogie.class, ID_ONLY);
        assertPropsAreFetched(fetchModel, Set.of("id"));
        assertPropsAreProxied(fetchModel, Set.of("version", "key", "location"));
    }
    
    @Test
    public void fetch_composite_key_of_synthetic_entity() {
        final IRetrievalModel<TgPublishedYearly> fetchModel = produceRetrievalModel(TgPublishedYearly.class, KEY_AND_DESC);
        assertPropsAreFetched(fetchModel, Set.of("author"));
        assertPropsAreProxied(fetchModel, Set.of("qty"));

        assertPropsAreNotFetched(fetchModel, Set.of("id", "version"));
        assertPropsAreNotProxied(fetchModel, Set.of("id"));

        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author"),
                              Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModels().get("author").getRetrievalModels().get("name"),
                              Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel.getRetrievalModels().get("author"),
                                 Set.of("pseudonym", "honorarium", "honorarium.amount"));
    }

    @Test
    public void default_strategy_implicitly_includes_calculated_properties_with_a_composite_type() {
        var model = produceRetrievalModel(TgVehicle.class, DEFAULT);
        assertPropsAreFetched(model, Set.of("sumOfPrices"));
    }

    /*----------------------------------------------------------------------------
     | Assert that fetch model construction terminates
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ALL),
                              Set.of("union"));
        assertPropsAreFetched(produceRetrievalModel(Circular_UnionEntity.class, ALL),
                              Set.of("entity"));
    }

    @Test
    public void strategy_DEFAULT_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, DEFAULT),
                Set.of("union"));
        assertPropsAreFetched(produceRetrievalModel(Circular_UnionEntity.class, DEFAULT),
                Set.of("entity"));
    }

    @Test
    public void strategy_ALL_INCL_CALC_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ALL_INCL_CALC),
                Set.of("union"));
        assertPropsAreFetched(produceRetrievalModel(Circular_UnionEntity.class, ALL_INCL_CALC),
                Set.of("entity"));
    }

    @Test
    public void strategy_KEY_AND_DESC_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, KEY_AND_DESC),
                Set.of("union"));
        assertPropsAreFetched(produceRetrievalModel(Circular_UnionEntity.class, KEY_AND_DESC),
                Set.of("entity"));
    }

    @Test
    public void strategy_ID_AND_VERSION_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreNotFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ID_AND_VERSION),
                Set.of("union"));
        assertPropsAreNotFetched(produceRetrievalModel(Circular_UnionEntity.class, ID_AND_VERSION),
                Set.of("entity"));
    }

    @Test
    public void strategy_ID_ONLY_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreNotFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ID_ONLY),
                Set.of("union"));
        assertPropsAreNotFetched(produceRetrievalModel(Circular_UnionEntity.class, ID_ONLY),
                Set.of("entity"));
    }

    @Test
    public void strategy_NONE_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertPropsAreNotFetched(produceRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, NONE),
                Set.of("union"));
        assertPropsAreNotFetched(produceRetrievalModel(Circular_UnionEntity.class, NONE),
                Set.of("entity"));
    }

}
