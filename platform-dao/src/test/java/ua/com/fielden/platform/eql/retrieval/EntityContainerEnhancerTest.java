package ua.com.fielden.platform.eql.retrieval;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// Tests for the enhancement of entity containers ([EntityContainerEnhancer]) of a reference that is retrieved together with the rows referencing it,
/// where several rows reference the same entity.
///
/// Queries constructed during fetching, unlike the main query, join entity-typed properties that have a fetch model of their own (see `ResultQuery1`),
/// so that each row carries its own container of the reference, populated from the same query.
/// Here, the main query retrieves fuel usages ([TeVehicleFuelUsage]), and the query for their key member `vehicle` is constructed during fetching.
/// Its rows are vehicles, and the reference is `replacedBy`, which two of them share.
/// Such containers are enhanced once per distinct entity, and then shared by all rows that reference it.
///
/// In any query, including the main one, each row also carries its own container of a union-typed property, populated with the IDs of union members from the same row.
///
/// These tests verify that sharing is confined to one property at one level of the fetch model,
/// so that the same entity, reached by another path, retains the fetch model of that path,
/// that a collectional property of the shared reference is retrieved,
/// and that a union-typed property of the main query is retrieved for every row, whichever union member is active.
///
public class EntityContainerEnhancerTest extends AbstractDaoTestCase {

    private static final String
            MAKE = "MERC",
            MODEL = "316",
            REPLACEMENT = "REPLACEMENT",
            REPLACED_1 = "REPLACED1",
            REPLACED_2 = "REPLACED2",
            WORKSHOP_1 = "WSHOP1",
            WORKSHOP_2 = "WSHOP2",
            WAGON = "WAGON1",
            BOGIE_IN_WORKSHOP_1_A = "BOGIE1",
            BOGIE_IN_WORKSHOP_1_B = "BOGIE2",
            BOGIE_IN_WORKSHOP_1_C = "BOGIE3",
            BOGIE_IN_WORKSHOP_2 = "BOGIE4",
            BOGIE_IN_WAGON_SLOT = "BOGIE5",
            BOGIE_WITHOUT_LOCATION = "BOGIE6";

    /// Fuel usages of the replacement vehicle reach it as `vehicle`, and fuel usages of the two replaced vehicles reach it as `vehicle.replacedBy`.
    /// The replaced vehicles share the replacement as `replacedBy`, so its containers on that path are enhanced once and shared.
    ///
    @Test
    public void the_same_entity_at_two_levels_retains_the_fetch_model_of_each_level_when_the_shared_reference_fetches_less() {
        final var fetchModel = fetchKeyAndDescOnly(TeVehicleFuelUsage.class)
                .with("vehicle", fetchKeyAndDescOnly(TeVehicle.class)
                        .with("replacedBy", fetchKeyAndDescOnly(TeVehicle.class).with("model", fetchKeyAndDescOnly(TeVehicleModel.class))));

        final var fuelUsages = co(TeVehicleFuelUsage.class).getAllEntities(from(select(TeVehicleFuelUsage.class).model()).with(fetchModel).model());

        assertThat(fuelUsages).hasSize(5);
        final var replacementAsVehicle = fuelUsages.stream().filter(usageOf(REPLACEMENT)).map(TeVehicleFuelUsage::getVehicle).toList();
        final var replacementAsReplacedBy = fuelUsages.stream().filter(usageOf(REPLACED_1).or(usageOf(REPLACED_2))).map(usage -> usage.getVehicle().getReplacedBy()).toList();
        assertThat(replacementAsVehicle).hasSize(2);
        assertThat(replacementAsReplacedBy).hasSize(3);

        for (final var vehicle : replacementAsVehicle) {
            assertThat(vehicle.getKey()).isEqualTo(REPLACEMENT);
            assertThat(hasModelFetched(vehicle)).isFalse();
            assertThat(replacementAsReplacedBy).allSatisfy(replacedBy -> assertThat(replacedBy).isNotSameAs(vehicle));
        }
        for (final var replacedBy : replacementAsReplacedBy) {
            assertThat(replacedBy.getKey()).isEqualTo(REPLACEMENT);
            assertThat(hasModelFetched(replacedBy)).isTrue();
            assertThat(replacedBy.getModel().getKey()).isEqualTo(MODEL);
        }
    }

    /// The same as above, but with the fetch models of the two paths swapped.
    ///
    @Test
    public void the_same_entity_at_two_levels_retains_the_fetch_model_of_each_level_when_the_shared_reference_fetches_more() {
        final var fetchModel = fetchKeyAndDescOnly(TeVehicleFuelUsage.class)
                .with("vehicle", fetchKeyAndDescOnly(TeVehicle.class)
                        .with("model", fetchKeyAndDescOnly(TeVehicleModel.class))
                        .with("replacedBy", fetchKeyAndDescOnly(TeVehicle.class)));

        final var fuelUsages = co(TeVehicleFuelUsage.class).getAllEntities(from(select(TeVehicleFuelUsage.class).model()).with(fetchModel).model());

        assertThat(fuelUsages).hasSize(5);
        final var replacementAsVehicle = fuelUsages.stream().filter(usageOf(REPLACEMENT)).map(TeVehicleFuelUsage::getVehicle).toList();
        final var replacementAsReplacedBy = fuelUsages.stream().filter(usageOf(REPLACED_1).or(usageOf(REPLACED_2))).map(usage -> usage.getVehicle().getReplacedBy()).toList();
        assertThat(replacementAsVehicle).hasSize(2);
        assertThat(replacementAsReplacedBy).hasSize(3);

        for (final var vehicle : replacementAsVehicle) {
            assertThat(hasModelFetched(vehicle)).isTrue();
            assertThat(vehicle.getModel().getKey()).isEqualTo(MODEL);
            assertThat(replacementAsReplacedBy).allSatisfy(replacedBy -> assertThat(replacedBy).isNotSameAs(vehicle));
        }
        for (final var replacedBy : replacementAsReplacedBy) {
            assertThat(replacedBy.getKey()).isEqualTo(REPLACEMENT);
            assertThat(hasModelFetched(replacedBy)).isFalse();
        }
    }

    /// The replaced vehicles share the replacement as `replacedBy`.
    /// Enhancing a collectional property requires distinct owners, which holds only if the containers of `replacedBy` are reduced to one per distinct ID.
    ///
    @Test
    public void collectional_property_of_a_reference_shared_by_several_rows_is_retrieved_for_each_row() {
        final var fetchModel = fetchKeyAndDescOnly(TeVehicleFuelUsage.class)
                .with("vehicle", fetchKeyAndDescOnly(TeVehicle.class)
                        .with("replacedBy", fetchKeyAndDescOnly(TeVehicle.class).with("fuelUsages", fetch(TeVehicleFuelUsage.class))));

        final var fuelUsages = co(TeVehicleFuelUsage.class).getAllEntities(
                from(select(TeVehicleFuelUsage.class).where().prop("vehicle.replacedBy.key").eq().val(REPLACEMENT).model()).with(fetchModel).model());

        assertThat(fuelUsages).hasSize(3);
        for (final var fuelUsage : fuelUsages) {
            assertThat(fuelUsage.getVehicle().getReplacedBy().getFuelUsages())
                    .extracting(TeVehicleFuelUsage::getQty)
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyInAnyOrder(new BigDecimal("100"), new BigDecimal("120"));
        }
    }

    /// The default fetch model retrieves union members as ID-only proxies, and each row of the main query still carries its own container of `location`.
    /// Three rows reference the same workshop, so that at least one of them retains its own container unless the enhanced container is assigned to all of them.
    /// The other rows ensure that the location of each row is determined by its own ID, including a different active union member and no location at all.
    ///
    @Test
    public void union_typed_property_of_the_main_query_is_retrieved_for_each_row_with_the_default_fetch_model() {
        final var bogies = bogiesByKey(fetch(TgBogie.class));

        assertThat(bogies).hasSize(6);
        final var sharedLocation = bogies.get(BOGIE_IN_WORKSHOP_1_A).getLocation();
        assertThat(sharedLocation.getWorkshop().isIdOnlyProxy()).isTrue();
        assertThat(sharedLocation.getWorkshop().getId()).isEqualTo(co(TgWorkshop.class).findByKey(WORKSHOP_1).getId());
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_1_B).getLocation()).isSameAs(sharedLocation);
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_1_C).getLocation()).isSameAs(sharedLocation);
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_2).getLocation().getWorkshop().getId()).isEqualTo(co(TgWorkshop.class).findByKey(WORKSHOP_2).getId());
        assertThat(bogies.get(BOGIE_IN_WAGON_SLOT).getLocation().getWorkshop()).isNull();
        assertThat(bogies.get(BOGIE_IN_WAGON_SLOT).getLocation().getWagonSlot()).isNotNull();
        assertThat(bogies.get(BOGIE_WITHOUT_LOCATION).getLocation()).isNull();
    }

    /// The same as above, but with fetch models for union members, which are applied to the location of every row.
    ///
    @Test
    public void union_typed_property_of_the_main_query_is_retrieved_for_each_row_with_fetch_models_for_union_members() {
        final var bogies = bogiesByKey(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class)
                .with("workshop", fetchKeyAndDescOnly(TgWorkshop.class))
                .with("wagonSlot", fetch(TgWagonSlot.class).with("wagon", fetchKeyAndDescOnly(TgWagon.class)))));

        assertThat(bogies).hasSize(6);
        final var sharedLocation = bogies.get(BOGIE_IN_WORKSHOP_1_A).getLocation();
        assertThat(sharedLocation.getWorkshop().getKey()).isEqualTo(WORKSHOP_1);
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_1_B).getLocation()).isSameAs(sharedLocation);
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_1_C).getLocation()).isSameAs(sharedLocation);
        assertThat(bogies.get(BOGIE_IN_WORKSHOP_2).getLocation().getWorkshop().getKey()).isEqualTo(WORKSHOP_2);
        assertThat(bogies.get(BOGIE_IN_WAGON_SLOT).getLocation().getWorkshop()).isNull();
        assertThat(bogies.get(BOGIE_IN_WAGON_SLOT).getLocation().getWagonSlot().getWagon().getKey()).isEqualTo(WAGON);
        assertThat(bogies.get(BOGIE_WITHOUT_LOCATION).getLocation()).isNull();
    }

    private Map<String, TgBogie> bogiesByKey(final fetch<TgBogie> fetchModel) {
        return co(TgBogie.class).getAllEntities(from(select(TgBogie.class).model()).with(fetchModel).model())
                .stream().collect(toMap(TgBogie::getKey, identity()));
    }

    /// Whether `model` was fetched with its own properties, rather than proxied or retrieved as an ID-only proxy.
    /// Key member `vehicle` of a fuel usage is retrieved with `model` as an ID-only proxy, unless the fetch model includes `model`.
    ///
    private static boolean hasModelFetched(final TeVehicle vehicle) {
        return !vehicle.proxiedPropertyNames().contains("model") && !vehicle.getModel().isIdOnlyProxy();
    }

    private static Predicate<TeVehicleFuelUsage> usageOf(final String vehicleKey) {
        return usage -> vehicleKey.equals(usage.getVehicle().getKey());
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
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final var fuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final var make = save(new_(TeVehicleMake.class, MAKE, "Mercedes"));
        final var model = save(new_(TeVehicleModel.class, MODEL, "Model 316").setMake(make));
        final var replacement = save(new_(TeVehicle.class, REPLACEMENT, "Replacement vehicle").setModel(model));
        final var replaced1 = save(new_(TeVehicle.class, REPLACED_1, "Replaced vehicle 1").setModel(model).setReplacedBy(replacement));
        final var replaced2 = save(new_(TeVehicle.class, REPLACED_2, "Replaced vehicle 2").setModel(model).setReplacedBy(replacement));

        save(new_composite(TeVehicleFuelUsage.class, replacement, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(fuelType));
        save(new_composite(TeVehicleFuelUsage.class, replacement, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(fuelType));
        save(new_composite(TeVehicleFuelUsage.class, replaced1, date("2005-01-01 00:00:00")).setQty(new BigDecimal("50")).setFuelType(fuelType));
        save(new_composite(TeVehicleFuelUsage.class, replaced1, date("2005-02-01 00:00:00")).setQty(new BigDecimal("60")).setFuelType(fuelType));
        save(new_composite(TeVehicleFuelUsage.class, replaced2, date("2005-03-01 00:00:00")).setQty(new BigDecimal("70")).setFuelType(fuelType));

        final var workshop1 = save(new_(TgWorkshop.class, WORKSHOP_1, "Workshop 1"));
        final var workshop2 = save(new_(TgWorkshop.class, WORKSHOP_2, "Workshop 2"));
        final var wagonSlot = save(new_composite(TgWagonSlot.class, save(new_(TgWagon.class, WAGON, "Wagon 1")), 1));
        save(new_(TgBogie.class, BOGIE_IN_WORKSHOP_1_A, "Bogie 1").setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop1)));
        save(new_(TgBogie.class, BOGIE_IN_WORKSHOP_1_B, "Bogie 2").setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop1)));
        save(new_(TgBogie.class, BOGIE_IN_WORKSHOP_1_C, "Bogie 3").setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop1)));
        save(new_(TgBogie.class, BOGIE_IN_WORKSHOP_2, "Bogie 4").setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop2)));
        save(new_(TgBogie.class, BOGIE_IN_WAGON_SLOT, "Bogie 5").setLocation(co$(TgBogieLocation.class).new_().setWagonSlot(wagonSlot)));
        save(new_(TgBogie.class, BOGIE_WITHOUT_LOCATION, "Bogie 6"));
    }

}
