package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.test_entities.Circular_EntityWithCompositeKeyMemberUnionEntity;
import ua.com.fielden.platform.entity.query.test_entities.Circular_UnionEntity;
import ua.com.fielden.platform.entity.query.test_entities.SynEntityWithYieldId;
import ua.com.fielden.platform.entity.query.test_entities.SynEntityWithoutYieldId;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertNotEmpty;

public class FetchModelTest extends AbstractDaoTestCase {

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    private <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final fetch<T> fetchModel) {
        return IRetrievalModel.createRetrievalModel(fetchModel,
                                                    domainMetadata,
                                                    getInstance(QuerySourceInfoProvider.class));
    }
    
    private <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(final Class<T> entityType, final FetchCategory fetchCategory) {
        return produceRetrievalModel(new fetch<T>(entityType, fetchCategory));
    }

    private <T extends AbstractEntity<?>> IRetrievalModel<T> produceRetrievalModel(
            final Class<T> entityType,
            final FetchCategory fetchCategory,
            final Function<? super fetch<T>, fetch<T>> finisher)
    {
        return produceRetrievalModel(finisher.apply(new fetch<T>(entityType, fetchCategory)));
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreFetched(final IRetrievalModel<T> fetchModel, final Iterable<String> props) {
        for (final String propName : props) {
            assertTrue(format("Property [%s] should be contained within fetch model:\n%s", propName, fetchModel), fetchModel.containsProp(propName));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreNotFetched(final IRetrievalModel<T> fetchModel, final Iterable<String> props) {
        for (final String prop : props) {
            assertFalse(format("Property [%s] should not be contained within fetch model:\n%s", prop, fetchModel),
                        fetchModel.containsProp(prop));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreProxied(final IRetrievalModel<T> fetchModel, final Iterable<String> proxiedProps) {
        for (final String propName : proxiedProps) {
            assertTrue(format("Property [%s] should be proxied within fetch model:\n%s", propName, fetchModel), fetchModel.containsProxy(propName));
        }
    }

    private static <T extends AbstractEntity<?>> void assertPropsAreNotProxied(final IRetrievalModel<T> fetchModel, final Iterable<String> props) {
        for (final String prop : props) {
            assertFalse(format("Property [%s] should not be proxied within fetch model:\n%s", prop, fetchModel),
                        fetchModel.containsProxy(prop));
        }
    }

    private static Stream<FetchCategory> allFetchCategories() {
        return Arrays.stream(FetchCategory.values());
    }

    /*----------------------------------------------------------------------------
     | Fetching of composite key members
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_composite_key_members_are_included_recursively() {
        _composite_key_members_are_included_recursively(ALL);
    }

    @Test
    public void strategy_ALL_INCL_CALC_composite_key_members_are_included_recursively() {
        _composite_key_members_are_included_recursively(ALL_INCL_CALC);
    }

    @Test
    public void strategy_DEFAULT_composite_key_members_are_included_recursively() {
        _composite_key_members_are_included_recursively(DEFAULT);
    }

    @Test
    public void strategy_KEY_AND_DESC_composite_key_members_are_included_recursively() {
        _composite_key_members_are_included_recursively(KEY_AND_DESC);
    }

    @Test
    public void strategy_ID_AND_VERSION_composite_key_members_are_not_included() {
        _composite_key_members_are_not_included(ID_AND_VERSION);
    }

    @Test
    public void strategy_ID_ONLY_composite_key_members_are_not_included() {
        _composite_key_members_are_not_included(ID_ONLY);
    }

    @Test
    public void strategy_NONE_composite_key_members_are_not_included() {
        _composite_key_members_are_not_included(NONE);
    }

    private void _composite_key_members_are_included_recursively(final FetchCategory category) {
        final var fetchModel = produceRetrievalModel(TgAuthorship.class, category);
        assertPropsAreFetched(fetchModel, Set.of("title", "author"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("author"), Set.of("name", "surname", "patronymic"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("author.name"), Set.of("key"));
    }

    private void _composite_key_members_are_not_included(final FetchCategory category) {
        final var fetchModel = produceRetrievalModel(TgAuthorship.class, category);
        assertPropsAreNotFetched(fetchModel, Set.of("author", "title"));
    }

    /*----------------------------------------------------------------------------
     | Fetching of composite key
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, ALL);
    }

    @Test
    public void strategy_ALL_INCL_CALC_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, ALL_INCL_CALC);
    }

    @Test
    public void strategy_DEFAULT_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, DEFAULT);
    }

    @Test
    public void strategy_KEY_AND_DESC_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, KEY_AND_DESC);
    }

    @Test
    public void strategy_ID_AND_VERSION_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, ID_AND_VERSION);
    }

    @Test
    public void strategy_ID_ONLY_composite_key_itself_is_not_included() {
        _composite_key_itself_is_not_included(TgFuelUsage.class, ID_ONLY);
    }

    @Test
    public void composite_key_itself_is_not_included_if_specified_explicitly() {
        final var entityType = TgFuelUsage.class;
        assertTrue(domainMetadata.forProperty(entityType, "key").type().isCompositeKey());

        final var fetchModel = produceRetrievalModel(fetchNone(entityType).with("key"));
        assertPropsAreNotFetched(fetchModel, Set.of("key"));
    }

    private void _composite_key_itself_is_not_included(
            final Class<? extends AbstractEntity<DynamicEntityKey>> entityType,
            final FetchCategory category)
    {
        final var fetchModel = produceRetrievalModel(entityType, category);
        assertPropsAreNotFetched(fetchModel, Set.of("key"));
    }

    /*----------------------------------------------------------------------------
     | Fetching of calculated properties
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_only_those_calculated_properties_that_have_component_type_are_included() {
        _only_those_calculated_properties_that_have_component_type_are_included(TgVehicle.class, ALL);
    }

    @Test
    public void strategy_DEFAULT_only_those_calculated_properties_that_have_component_type_are_included() {
        _only_those_calculated_properties_that_have_component_type_are_included(TgVehicle.class, DEFAULT);
    }

    @Test
    public void strategy_ALL_INCL_CALC_all_calculated_properties_are_included() {
        _all_calculated_properties_are_included(TgAuthor.class, ALL_INCL_CALC);
    }

    @Test
    public void strategy_KEY_AND_DESC_all_calculated_properties_are_excluded() {
        _all_calculated_properties_are_excluded(TgAuthor.class, KEY_AND_DESC);
    }

    @Test
    public void strategy_ID_ONLY_all_calculated_properties_are_excluded() {
        _all_calculated_properties_are_excluded(TgAuthor.class, ID_ONLY);
    }

    @Test
    public void strategy_NONE_all_calculated_properties_are_excluded() {
        _all_calculated_properties_are_excluded(TgAuthor.class, NONE);
    }

    private void _only_those_calculated_properties_that_have_component_type_are_included(
            final Class<? extends AbstractEntity<?>> entityType,
            final FetchCategory category)
    {
        final var fetchModel = produceRetrievalModel(entityType, category);
        domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCalculated).flatMap(Optional::stream)
                .forEach(prop -> {
                    if (prop.type().isComponent()) {
                        assertPropsAreFetched(fetchModel, Set.of(prop.name()));
                    } else {
                        assertPropsAreNotFetched(fetchModel, Set.of(prop.name()));
                    }
                });
    }

    private void _all_calculated_properties_are_included(
            final Class<? extends AbstractEntity<?>> entityType,
            final FetchCategory category)
    {
        final var fetchModel = produceRetrievalModel(entityType, category);
        final var calculatedProps = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCalculated).flatMap(Optional::stream)
                // Composite key itself is never included. See the documentation of EntityRetrievalModel.
                .filter(prop -> !prop.type().isCompositeKey())
                .map(PropertyMetadata::name)
                .toList();
        assertPropsAreFetched(fetchModel, calculatedProps);
    }

    private void _all_calculated_properties_are_excluded(
            final Class<? extends AbstractEntity<?>> entityType,
            final FetchCategory category)
    {
        final var fetchModel = produceRetrievalModel(entityType, category);
        final var calculatedProps = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCalculated).flatMap(Optional::stream)
                .map(PropertyMetadata::name)
                .toList();
        assertPropsAreNotFetched(fetchModel, calculatedProps);
    }

    /*----------------------------------------------------------------------------
     | Fetching of property "id" in synthetic entities
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, ALL), Set.of("id"));
    }

    @Test
    public void strategy_ALL_INCL_CALC_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, ALL_INCL_CALC), Set.of("id"));
    }

    @Test
    public void strategy_DEFAULT_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, DEFAULT), Set.of("id"));
    }

    @Test
    public void strategy_KEY_AND_DESC_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, KEY_AND_DESC), Set.of("id"));
    }

    @Test
    public void strategy_ID_ONLY_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, ID_ONLY), Set.of("id"));
    }

    // ID_ONLY uncoditionally includes ID
    @Test
    public void strategy_ID_ONLY_id_is_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, ID_ONLY), Set.of("id"));
    }

    @Test
    public void strategy_ID_AND_VERSION_id_is_included_if_synthetic_model_yields_into_it() {
        assertPropsAreFetched(produceRetrievalModel(SynEntityWithYieldId.class, ID_AND_VERSION), Set.of("id"));
    }

    @Test
    public void strategy_ALL_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreNotFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, ALL), Set.of("id"));
    }

    @Test
    public void strategy_ALL_INCL_CALC_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreNotFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, ALL_INCL_CALC), Set.of("id"));
    }

    @Test
    public void strategy_DEFAULT_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreNotFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, DEFAULT), Set.of("id"));
    }

    @Test
    public void strategy_KEY_AND_DESC_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreNotFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, KEY_AND_DESC), Set.of("id"));
    }

    @Test
    public void strategy_ID_AND_VERSION_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertPropsAreNotFetched(produceRetrievalModel(SynEntityWithoutYieldId.class, ID_AND_VERSION), Set.of("id"));
    }

    // END SECTION

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
        assertPropsAreFetched(fetchModel.getRetrievalModel("make"), Set.of("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_model() {
        final IRetrievalModel<TgVehicleModel> fetchModel = produceRetrievalModel(TgVehicleModel.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("key", "desc", "id", "version", "make"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("make"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("make"), Set.of("version", "key", "desc"));
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

        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle.model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("vehicle.model"), Set.of("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModel("fuelType"), Set.of("id", "version", "key", "desc"));
    }

    @Test
    public void test_minimal_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "date", "qty"));

        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle.model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("vehicle.model"), Set.of("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModel("fuelType"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("fuelType"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_key_and_desc_fetching_of_fuel_usage() {
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(TgFuelUsage.class, KEY_AND_DESC);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle", "date"));
        
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle.model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("vehicle.model"), Set.of("version", "key", "desc"));
       
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
        
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle.model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("vehicle.model"), Set.of("version", "key", "desc"));

        assertPropsAreFetched(fetchModel.getRetrievalModel("fuelType"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("fuelType"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_id_and_version_fetching_of_fuel_usage_with_vehicle() {
        final fetch<TgFuelUsage> fetch = new fetch<>(TgFuelUsage.class, ID_AND_VERSION).with("vehicle");
        final IRetrievalModel<TgFuelUsage> fetchModel = produceRetrievalModel(fetch);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "vehicle"));
        assertPropsAreProxied(fetchModel, Set.of("qty", "date", "fuelType"));

        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle"), Set.of("id", "version", "key", "desc", "model"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("vehicle.model"), Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("vehicle.model"), Set.of("version", "key", "desc"));
    }

    @Test
    public void test_all_fetching_of_bogie() {
        final IRetrievalModel<TgBogie> fetchModel = produceRetrievalModel(TgBogie.class, ALL);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "key", "desc", "location"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("location"), Set.of("workshop", "wagonSlot"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("location.workshop"),
                              Set.of("id", "version", "key", "desc"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("location.wagonSlot"),
                              Set.of("id", "version", "wagon", "position"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("location.wagonSlot.wagon"),
                              Set.of("id", "version", "key", "desc", "serialNo", "wagonClass"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("location.wagonSlot.wagon.wagonClass"),
                              Set.of("id"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("location.wagonSlot.wagon.wagonClass"),
                              Set.of("version", "key", "desc", "numberOfBogies", "numberOfWheelsets", "tonnage"));
    }

    @Test
    public void test_virtual_composite_key_property() {
        final IRetrievalModel<TgAuthor> fetchModel = produceRetrievalModel(TgAuthor.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("name"), Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel, Set.of("pseudonym", "honorarium", "honorarium.amount"));
    }

    @Test
    public void strategy_DEFAULT_for_synthetic_entity() {
        final IRetrievalModel<TgAverageFuelUsage> fetchModel = produceRetrievalModel(TgAverageFuelUsage.class, DEFAULT);
        assertPropsAreFetched(fetchModel, Set.of("key", "qty", "cost"));
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
        assertPropsAreFetched(fetchModel.getRetrievalModel("station"), Set.of("id", "version"));
        assertPropsAreProxied(fetchModel.getRetrievalModel("station"), Set.of("parent", "name", "fuelType"));

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

        assertPropsAreFetched(fetchModel.getRetrievalModel("author"),
                              Set.of("id", "version", "name", "surname", "patronymic", "dob", "utcDob", "webpage"));
        assertPropsAreFetched(fetchModel.getRetrievalModel("author").getRetrievalModel("name"),
                              Set.of("id", "version", "key", "desc"));
        assertPropsAreNotFetched(fetchModel.getRetrievalModel("author"),
                                 Set.of("pseudonym", "honorarium", "honorarium.amount"));
    }

    @Test
    public void version_is_never_proxied_for_synthetic_based_on_persistent_entities() {
        final var entityType = TgReVehicleModel.class;
        assertTrue(EntityUtils.isSyntheticBasedOnPersistentEntityType(entityType));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, NONE), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, ID_ONLY), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, ID_AND_VERSION), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, DEFAULT), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, KEY_AND_DESC), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, ALL_INCL_CALC), Set.of(VERSION));
        assertPropsAreNotProxied(produceRetrievalModel(entityType, ALL), Set.of(VERSION));
    }

    @Test
    public void critOnly_properties_are_never_included_implicitly() {
        final var entityType = TgVehicle.class;
        final var critOnlyPropNames = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCritOnly)
                .flatMap(Optional::stream)
                .map(PropertyMetadata::name)
                .toList();
        assertNotEmpty(critOnlyPropNames);

        allFetchCategories()
                .map(cat -> produceRetrievalModel(entityType, cat))
                .forEach(model -> assertPropsAreNotFetched(model, critOnlyPropNames));
    }

    @Test
    public void critOnly_properties_are_included_if_specified_explicitly() {
        final var entityType = TgVehicle.class;
        final var critOnlyPropNames = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCritOnly)
                .flatMap(Optional::stream)
                .map(PropertyMetadata::name)
                .toList();
        assertNotEmpty(critOnlyPropNames);

        allFetchCategories()
                .map(cat -> produceRetrievalModel(entityType, cat, fetch -> fetch.with(critOnlyPropNames)))
                .forEach(model -> assertPropsAreFetched(model, critOnlyPropNames));
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

    /*----------------------------------------------------------------------------
     | Implicit fetching of calculated `desc`
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_KEY_AND_DESC_calculated_desc_is_included() {
        _calculated_desc_is_included(KEY_AND_DESC);
    }

    @Test
    public void strategy_DEFAULT_calculated_desc_is_included() {
        _calculated_desc_is_included(DEFAULT);
    }

    @Test
    public void strategy_ALL_calculated_desc_is_included() {
        _calculated_desc_is_included(ALL);
    }

    private void _calculated_desc_is_included(final FetchCategory category) {
        final var entityMetadata = domainMetadata.forEntity(EntityWithRichText.class);
        assertTrue(entityMetadata.hasProperty(DESC));
        assertTrue(entityMetadata.property(DESC).isCalculated());
        assertPropsAreFetched(produceRetrievalModel(EntityWithRichText.class, category),
                              Set.of(DESC));
    }

    @Override
    protected void populateDomain() {}

}
