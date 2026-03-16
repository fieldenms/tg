package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage1.sundries.ExpandMoneyTypedYield1;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;

/// A denotational test for [ExpandMoneyTypedYield1].
///
public class ExpandMoneyTypedYield1Test extends EqlStage2TestCase {

    @Test
    public void test_entities_have_expected_Hibernate_types() {
        assertThat(((ICompositeUserTypeInstantiate) metadata().forProperty(TgVehicle.class, "price").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT);
        assertThat(((ICompositeUserTypeInstantiate) metadata().forProperty(TgFuelUsage.class, "pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);
        assertThat(((ICompositeUserTypeInstantiate) metadata().forProperty(TgMeterReading.class, "fuelUsage.pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);
    }

    @Test
    public void money_typed_prop_yield_is_transformed_into_yields_for_all_components_01() {
        final var query1 = select(TgVehicle.class)
                .yield().prop("price").as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgVehicle.class)
                .yield().prop("price.amount").as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void money_typed_prop_yield_is_transformed_into_yields_for_all_components_02() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre").as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre.amount").as("pricePerLitre.amount")
                .yield().prop("pricePerLitre.currency").as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void money_typed_prop_path_yield_is_transformed_into_yields_for_all_components_01() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("vehicle.price").as("vehicle.price")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("vehicle.price.amount").as("vehicle.price.amount")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void money_typed_prop_path_yield_is_transformed_into_yields_for_all_components_02() {
        final var query1 = select(TgMeterReading.class)
                .yield().prop("fuelUsage.pricePerLitre").as("fuelUsage.pricePerLitre")
                .modelAsEntity(TgMeterReading.class);

        final var query2 = select(TgMeterReading.class)
                .yield().prop("fuelUsage.pricePerLitre.amount").as("fuelUsage.pricePerLitre.amount")
                .yield().prop("fuelUsage.pricePerLitre.currency").as("fuelUsage.pricePerLitre.currency")
                .modelAsEntity(TgMeterReading.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void alias_of_a_yield_of_expr_into_a_Money_typed_property_is_transformed_into_an_alias_for_amount_01() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void alias_of_a_yield_of_expr_into_a_Money_typed_property_is_transformed_into_an_alias_for_amount_02() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("pricePerLitre.amount")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_money_typed_prop_with_amount_and_currency_into_a_money_typed_prop_with_amount() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre").as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre.amount").as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_money_typed_prop_with_amount_into_a_money_typed_prop_with_amount_and_currency() {
        final var query1 = select(TgVehicle.class)
                .yield().prop("price").as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgVehicle.class)
                .yield().prop("price.amount").as("pricePerLitre.amount")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }


}
