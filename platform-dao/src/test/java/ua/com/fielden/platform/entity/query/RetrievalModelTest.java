package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.exceptions.EntityRetrievalModelException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.test_entities.Circular_EntityWithCompositeKeyMemberUnionEntity;
import ua.com.fielden.platform.entity.query.test_entities.Circular_UnionEntity;
import ua.com.fielden.platform.entity.query.test_entities.SynEntityWithYieldId;
import ua.com.fielden.platform.entity.query.test_entities.SynEntityWithoutYieldId;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertNotEmpty;

public class RetrievalModelTest extends AbstractDaoTestCase implements IRetrievalModelTestUtils {

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

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
        assertRetrievalModel(TgAuthorship.class, category)
                .contains("title", "author")
                .subModel("author", a -> a.contains("name", "surname", "patronymic"))
                .subModel("author.name", a -> a.contains("key"));
    }

    private void _composite_key_members_are_not_included(final FetchCategory category) {
        assertRetrievalModel(TgAuthorship.class, category)
                .notContains("author", "title");
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

        assertRetrievalModel(entityType, NONE, f -> f.with("key"))
                .notContains("key");
    }

    private void _composite_key_itself_is_not_included(
            final Class<? extends AbstractEntity<DynamicEntityKey>> entityType,
            final FetchCategory category)
    {
        assertRetrievalModel(entityType, category)
                .notContains("key");
    }

    /*----------------------------------------------------------------------------
     | Fetching of simple entity-typed key (one-to-one association)
     -----------------------------------------------------------------------------*/

    @Test
    public void simple_entity_typed_key_is_implicitly_assigned_a_sub_fetch_model() {
        assertThat(List.of(ALL, ALL_INCL_CALC, DEFAULT, KEY_AND_DESC))
                .allSatisfy(category -> _simple_entity_typed_key_is_implicitly_assigned_a_sub_fetch_model(
                        TgVehicleFinDetails.class, category, a -> a.equalsModel(KEY_AND_DESC)));

        assertThat(List.of(ID_AND_VERSION, ID_ONLY))
                .allSatisfy(category -> assertRetrievalModel(TgVehicleFinDetails.class, category).notContains(KEY));
    }

    private void _simple_entity_typed_key_is_implicitly_assigned_a_sub_fetch_model(
            final Class<? extends AbstractEntity<? extends AbstractEntity<?>>> entityType,
            final FetchCategory category,
            final Consumer<RetrievalModelAssert<IRetrievalModel<?>, ?>> assertSubModel)
    {
        assertTrue(domainMetadata.forProperty(entityType, KEY).type().isEntity());

        assertRetrievalModel(entityType, category)
                .contains(KEY)
                .subModel(KEY, assertSubModel);
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
        final var partitions = domainMetadata.forEntity(entityType)
                .properties()
                .stream()
                .map(PropertyMetadata::asCalculated)
                .flatMap(Optional::stream)
                .collect(partitioningBy(prop -> prop.type().isComponent(),
                                        mapping(PropertyMetadata::name, toList())));
        final var componentTypedCalcProps = partitions.get(true);
        final var otherTypedCalcProps = partitions.get(false);

        assertRetrievalModel(entityType, category)
                .contains(componentTypedCalcProps)
                .notContains(otherTypedCalcProps);
    }

    private void _all_calculated_properties_are_included(
            final Class<? extends AbstractEntity<?>> entityType,
            final FetchCategory category)
    {
        final var calculatedProps = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCalculated).flatMap(Optional::stream)
                // Composite key itself is never included. See the documentation of EntityRetrievalModel.
                .filter(prop -> !prop.type().isCompositeKey())
                .map(PropertyMetadata::name)
                .toList();
        assertRetrievalModel(entityType, category)
                .contains(calculatedProps);
    }

    private void _all_calculated_properties_are_excluded(
            final Class<? extends AbstractEntity<?>> entityType,
            final FetchCategory category)
    {
        final var calculatedProps = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCalculated).flatMap(Optional::stream)
                .map(PropertyMetadata::name)
                .toList();
        assertRetrievalModel(entityType, category).notContains(calculatedProps);
    }

    /*----------------------------------------------------------------------------
     | Fetching of property "id" in synthetic entities
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, ALL).contains(ID);
    }

    @Test
    public void strategy_ALL_INCL_CALC_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, ALL_INCL_CALC).contains("id");
    }

    @Test
    public void strategy_DEFAULT_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, DEFAULT).contains("id");
    }

    @Test
    public void strategy_KEY_AND_DESC_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, KEY_AND_DESC).contains("id");
    }

    @Test
    public void strategy_ID_ONLY_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, ID_ONLY).contains("id");
    }

    // ID_ONLY uncoditionally includes ID
    @Test
    public void strategy_ID_ONLY_id_is_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, ID_ONLY).contains("id");
    }

    @Test
    public void strategy_ID_AND_VERSION_id_is_included_if_synthetic_model_yields_into_it() {
        assertRetrievalModel(SynEntityWithYieldId.class, ID_AND_VERSION).contains("id");
    }

    @Test
    public void strategy_ALL_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, ALL).notContains("id");
    }

    @Test
    public void strategy_ALL_INCL_CALC_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, ALL_INCL_CALC).notContains("id");
    }

    @Test
    public void strategy_DEFAULT_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, DEFAULT).notContains("id");
    }

    @Test
    public void strategy_KEY_AND_DESC_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, KEY_AND_DESC).notContains("id");
    }

    @Test
    public void strategy_ID_AND_VERSION_id_is_not_included_if_synthetic_model_does_not_yield_into_it() {
        assertRetrievalModel(SynEntityWithoutYieldId.class, ID_AND_VERSION).notContains("id");
    }

    /*----------------------------------------------------------------------------
     | Applying specific fetch categories to specific entity types
     -----------------------------------------------------------------------------*/

    @Test
    public void fetch_TgVehicleMake() {
        assertThat(List.of(ALL_INCL_CALC, ALL, DEFAULT, KEY_AND_DESC))
                .allSatisfy(cat -> assertRetrievalModel(TgVehicleMake.class, cat)
                        .containsExactly(KEY, DESC, ID, VERSION));
        assertRetrievalModel(TgVehicleMake.class, ID_AND_VERSION)
                .containsExactly(ID, VERSION)
                .proxiesExactly(KEY, DESC);
        assertRetrievalModel(TgVehicleMake.class, ID_ONLY)
                .containsExactly(ID)
                .proxiesExactly(KEY, DESC, VERSION);
        assertRetrievalModel(TgVehicleMake.class, NONE)
                .containsExactly()
                .proxiesExactly(KEY, DESC, VERSION);
    }

    @Test
    public void fetch_TgVehicleModel() {
        assertRetrievalModel(TgVehicleModel.class, ALL_INCL_CALC)
                .containsExactly(KEY, DESC, ID, VERSION, "make", "makeModelsCount")
                .subModel("make", a -> a.equalsModel(DEFAULT));
        assertRetrievalModel(TgVehicleModel.class, ALL)
                .containsExactly(KEY, DESC, ID, VERSION, "make")
                .subModel("make", a -> a.equalsModel(DEFAULT))
                .proxiesExactly("makeModelsCount");
        assertRetrievalModel(TgVehicleModel.class, DEFAULT)
                .containsExactly(KEY, DESC, ID, VERSION, "make")
                .subModel("make", a -> a.equalsModel(ID_ONLY))
                .proxiesExactly("makeModelsCount");
    }

    @Test
    public void fetch_TgFuelUsage() {
        assertRetrievalModel(TgFuelUsage.class, ALL)
                .containsExactly(ID, VERSION, "vehicle", "date", "qty", "pricePerLitre", "pricePerLitre.amount", "fuelType")
                .subModel("vehicle", a -> a.equalsModel(DEFAULT))
                .subModel("fuelType", a -> a.equalsModel(DEFAULT));
        assertRetrievalModel(TgFuelUsage.class, DEFAULT)
                .containsExactly(ID, VERSION, "vehicle", "date", "qty", "pricePerLitre", "pricePerLitre.amount", "fuelType")
                .subModel("vehicle", a -> a.equalsModel(DEFAULT))
                .subModel("fuelType", a -> a.equalsModel(ID_ONLY));
        assertRetrievalModel(TgFuelUsage.class, KEY_AND_DESC)
                .containsExactly(ID, VERSION, "vehicle", "date")
                .proxiesExactly("qty", "pricePerLitre", "fuelType")
                .subModel("vehicle", a -> a.equalsModel(DEFAULT));
        assertRetrievalModel(TgFuelUsage.class, ID_AND_VERSION, f -> f.with("vehicle", "qty"))
                .containsExactly(ID, VERSION, "vehicle", "qty")
                .proxiesExactly("date", "pricePerLitre", "fuelType")
                .subModel("vehicle", a -> a.equalsModel(DEFAULT));
        assertRetrievalModel(TgFuelUsage.class, DEFAULT, f -> f.without("vehicle", "qty"))
                .containsExactly(ID, VERSION, "date", "pricePerLitre", "pricePerLitre.amount", "fuelType")
                .proxiesExactly("vehicle", "qty");
    }

    @Test
    public void fetch_TgBogie() {
        assertRetrievalModel(TgBogie.class, ALL)
                .containsExactly(ID, VERSION, KEY, DESC,
                                 LAST_UPDATED_DATE, LAST_UPDATED_BY, LAST_UPDATED_TRANSACTION_GUID,
                                 CREATED_DATE, CREATED_BY, CREATED_TRANSACTION_GUID,
                                 ACTIVE, REF_COUNT,
                                 "location", "bogieClass")
                .subModel("location", a -> a.containsExactly(DESC, "wagonSlot", "workshop"))
                .subModel("location.wagonSlot", a -> a.equalsModel(DEFAULT))
                .subModel("location.workshop", a -> a.equalsModel(DEFAULT))
                .subModel("bogieClass", a -> a.equalsModel(DEFAULT));

        assertRetrievalModel(TgBogie.class, ID_ONLY, f -> f.with("location", fetchNone(TgBogieLocation.class).with("wagonSlot")))
                .containsExactly(ID, "location")
                .subModel("location", a -> a.containsExactly("wagonSlot").proxiesExactly("workshop", "fuelType", "desc"))
                .subModel("location.wagonSlot", a -> a.equalsModel(DEFAULT));
    }

    @Test
    public void fetch_synthetic_one_to_one_entity() {
        assertRetrievalModel(TgAverageFuelUsage.class, ALL_INCL_CALC)
                .containsExactly(ID, KEY, "qty", "cost", "cost.amount")
                .proxiesExactly();
        assertRetrievalModel(TgAverageFuelUsage.class, ALL)
                // TODO: Should contain ID.
                .containsExactly(KEY, "qty", "cost", "cost.amount")
                .proxiesExactly();
        assertRetrievalModel(TgAverageFuelUsage.class, DEFAULT)
                // TODO: Should contain ID.
                .containsExactly(KEY, "qty", "cost", "cost.amount")
                .proxiesExactly();
        assertRetrievalModel(TgAverageFuelUsage.class, KEY_AND_DESC)
                .containsExactly(ID, KEY)
                .proxiesExactly("qty", "cost");
        assertRetrievalModel(TgAverageFuelUsage.class, ID_AND_VERSION)
                .containsExactly(ID)
                .proxiesExactly("qty", "cost");
        assertRetrievalModel(TgAverageFuelUsage.class, ID_ONLY)
                .containsExactly(ID)
                .proxiesExactly("qty", "cost");
    }

    // END SECTION

    @Test
    public void ALL_includes_only_those_calculated_properties_that_have_component_type() {
        assertRetrievalModel(TgVehicle.class, ALL)
                .contains("sumOfPrices")
                .proxies("lastFuelUsage", "constValueProp", "calc0", "calcModel");
    }

    @Test
    public void DEFAULT_includes_only_those_calculated_properties_that_have_component_type() {
        assertRetrievalModel(TgVehicle.class, DEFAULT)
                .contains("sumOfPrices")
                .proxies("lastFuelUsage", "constValueProp", "calc0", "calcModel");
    }

    @Test
    public void fetch_composite_key_of_synthetic_entity() {
        assertRetrievalModel(TgPublishedYearly.class, KEY_AND_DESC)
                .containsExactly("author")
                .proxiesExactly("qty")
                .subModel("author", a -> a.equalsModel(DEFAULT));
    }

    @Test
    public void version_is_never_proxied_for_synthetic_based_on_persistent_entities() {
        final var entityType = TgReVehicleModel.class;
        assertTrue(EntityUtils.isSyntheticBasedOnPersistentEntityType(entityType));
        assertThat(allFetchCategories())
                .allSatisfy(cat -> assertRetrievalModel(entityType, cat).notProxies(VERSION));
        assertRetrievalModel(entityType, DEFAULT, f -> f.without(VERSION)).notProxies(VERSION);
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

        assertThat(allFetchCategories())
                .allSatisfy(cat -> assertRetrievalModel(entityType, cat)
                        .notContains(critOnlyPropNames));
    }

    @Test
    public void critOnly_properties_are_never_proxied() {
        final var entityType = TgVehicle.class;
        final var critOnlyPropNames = domainMetadata.forEntity(entityType).properties().stream()
                .map(PropertyMetadata::asCritOnly)
                .flatMap(Optional::stream)
                .map(PropertyMetadata::name)
                .toList();
        assertNotEmpty(critOnlyPropNames);

        assertThat(allFetchCategories())
                .allSatisfy(cat -> assertRetrievalModel(entityType, cat)
                        .notProxies(critOnlyPropNames));
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

        assertThat(allFetchCategories())
                .allSatisfy(cat -> assertRetrievalModel(entityType, cat, f -> f.with(critOnlyPropNames))
                        .contains(critOnlyPropNames));
    }

    /*----------------------------------------------------------------------------
     | Recursive key structure with a union-typed key member.
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_ALL_cannot_be_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertThatThrownBy(() -> makeRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ALL))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
        assertThatThrownBy(() -> makeRetrievalModel(Circular_UnionEntity.class, ALL))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
    }

    @Test
    public void strategy_DEFAULT_cannot_be_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertThatThrownBy(() -> makeRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, DEFAULT))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
        assertThatThrownBy(() -> makeRetrievalModel(Circular_UnionEntity.class, DEFAULT))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
    }

    @Test
    public void strategy_ALL_INCL_CALC_cannot_be_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertThatThrownBy(() -> makeRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ALL_INCL_CALC))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
        assertThatThrownBy(() -> makeRetrievalModel(Circular_UnionEntity.class, ALL_INCL_CALC))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
    }

    @Test
    public void strategy_KEY_AND_DESC_cannot_be_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertThatThrownBy(() -> makeRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, KEY_AND_DESC))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
        assertThatThrownBy(() -> makeRetrievalModel(Circular_UnionEntity.class, KEY_AND_DESC))
                .isInstanceOf(EntityRetrievalModelException.GraphCycle.class);
    }

    @Test
    public void strategy_ID_AND_VERSION_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ID_AND_VERSION)
                .notContains("union");
        assertRetrievalModel(Circular_UnionEntity.class, ID_AND_VERSION)
                .notContains("entity");
    }

    @Test
    public void strategy_ID_ONLY_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, ID_ONLY)
                .notContains("union");
        assertRetrievalModel(Circular_UnionEntity.class, ID_ONLY)
                .notContains("entity");
    }

    @Test
    public void strategy_NONE_is_constructed_for_circular_relationship_between_entity_with_composite_key_and_union_entity() {
        assertRetrievalModel(Circular_EntityWithCompositeKeyMemberUnionEntity.class, NONE)
                .notContains("union");
        assertRetrievalModel(Circular_UnionEntity.class, NONE)
                .notContains("entity");
    }

    /*----------------------------------------------------------------------------
     | Union-typed key member
     -----------------------------------------------------------------------------*/

    @Test
    public void union_typed_key_members_are_included_if_key_is_included() {
        assertThat(List.of(ALL_INCL_CALC, ALL, DEFAULT, KEY_AND_DESC))
                .allSatisfy(cat -> assertRetrievalModel(UnionEntityDetails.class, cat)
                        .contains(UnionEntityDetails.Property.serial, UnionEntityDetails.Property.union));
        assertRetrievalModel(UnionEntityDetails.class, NONE, f -> f.with(KEY))
                .contains(UnionEntityDetails.Property.serial, UnionEntityDetails.Property.union);
        assertThat(List.of(ID_ONLY, ID_AND_VERSION, NONE))
                .allSatisfy(cat -> assertRetrievalModel(UnionEntityDetails.class, cat)
                        .proxies(UnionEntityDetails.Property.serial, UnionEntityDetails.Property.union));
    }

    /*----------------------------------------------------------------------------
     | Union members
     -----------------------------------------------------------------------------*/

    @Test
    public void strategy_KEY_AND_DESC_union_members_are_included_with_DEFAULT_category() {
        _union_members_are_included_with_DEFAULT_category(KEY_AND_DESC);
    }

    @Test
    public void strategy_DEFAULT_union_members_are_included_with_DEFAULT_category() {
        _union_members_are_included_with_DEFAULT_category(DEFAULT);
    }

    @Test
    public void strategy_ALL_union_members_are_included_with_DEFAULT_category() {
        _union_members_are_included_with_DEFAULT_category(ALL);
    }

    @Test
    public void strategy_ALL_INCL_CALC_union_members_are_included_with_DEFAULT_category() {
        _union_members_are_included_with_DEFAULT_category(ALL_INCL_CALC);
    }

    private void _union_members_are_included_with_DEFAULT_category(final FetchCategory category) {
        final var entityMetadata = domainMetadata.forEntity(UnionEntity.class);
        assertThat(entityMetadata).matches(EntityMetadata::isUnion);
        final var retrievalModel = makeRetrievalModel(UnionEntity.class, category);
        assertThat(List.of(UnionEntity.Property.propertyOne, UnionEntity.Property.propertyTwo))
                .allSatisfy(prop -> assertThat(retrievalModel.getRetrievalModelOpt(prop))
                        .get()
                        .isInstanceOfSatisfying(EntityRetrievalModel.class,
                                                it -> assertThat(it.getOriginalFetch())
                                                        .extracting(fetch::getFetchCategory)
                                                        .isEqualTo(DEFAULT)));
    }

    @Test
    public void strategy_ID_ONLY_union_members_are_not_included() {
        _union_members_are_not_included(ID_ONLY);
    }

    @Test
    public void strategy_ID_AND_VERSION_union_members_are_not_included() {
        _union_members_are_not_included(ID_AND_VERSION);
    }

    @Test
    public void strategy_NONE_union_members_are_not_included() {
        _union_members_are_not_included(NONE);
    }

    private void _union_members_are_not_included(final FetchCategory category) {
        final var entityMetadata = domainMetadata.forEntity(UnionEntity.class);
        assertThat(entityMetadata).matches(EntityMetadata::isUnion);
        assertRetrievalModel(UnionEntity.class, category)
                .notContains(UnionEntity.Property.propertyOne, UnionEntity.Property.propertyTwo);
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
        assertRetrievalModel(EntityWithRichText.class, category)
                .contains(DESC);
    }

    @Override
    protected void populateDomain() {}

    /*----------------------------------------------------------------------------
     | Utilities
     -----------------------------------------------------------------------------*/

    private static Stream<FetchCategory> allFetchCategories() {
        return Arrays.stream(FetchCategory.values());
    }

}
