package ua.com.fielden.platform.eql.retrieval;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.math.BigDecimal;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// Tests for the enhancement of entity containers ([EntityContainerEnhancer]) of a reference that is retrieved together with the rows referencing it,
/// where several rows reference the same entity.
///
/// Entity-typed key members (and union members) are such references:
/// each row carries its own container of the reference, populated from the same query.
/// Here, the rows are fuel usages ([TeVehicleFuelUsage]), and the reference is key member `vehicle`.
/// Such containers are enhanced once per distinct entity, and then shared by all rows that reference it.
///
/// These tests verify that sharing is confined to one property at one level of the fetch model,
/// so that the same entity, reached by another path, retains the fetch model of that path,
/// and that a collectional property of the shared reference is retrieved.
///
public class EntityContainerEnhancerTest extends AbstractDaoTestCase {

    private static final String
            MAKE = "MERC",
            MODEL = "316",
            REPLACEMENT = "REPLACEMENT",
            REPLACED_1 = "REPLACED1",
            REPLACED_2 = "REPLACED2";

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

    @Test
    public void collectional_property_of_a_reference_shared_by_several_rows_is_retrieved_for_each_row() {
        final var fetchModel = fetchKeyAndDescOnly(TeVehicleFuelUsage.class)
                .with("vehicle", fetchKeyAndDescOnly(TeVehicle.class).with("fuelUsages", fetch(TeVehicleFuelUsage.class)));

        final var fuelUsages = co(TeVehicleFuelUsage.class).getAllEntities(
                from(select(TeVehicleFuelUsage.class).where().prop("vehicle.key").eq().val(REPLACEMENT).model()).with(fetchModel).model());

        assertThat(fuelUsages).hasSize(2);
        for (final var fuelUsage : fuelUsages) {
            assertThat(fuelUsage.getVehicle().getFuelUsages())
                    .extracting(TeVehicleFuelUsage::getQty)
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyInAnyOrder(new BigDecimal("100"), new BigDecimal("120"));
        }
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
    }

}
