package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage1.sundries.ExpandMoneyTypedYield1;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.Money.*;

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
                .yield().expr(expr().prop("pricePerLitre.currency").model()).as("pricePerLitre.currency")
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
                .yield().expr(expr().prop("fuelUsage.pricePerLitre.currency").model()).as("fuelUsage.pricePerLitre.currency")
                .modelAsEntity(TgMeterReading.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_inside_expr_is_transformed_into_yields_for_all_components_01() {
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
    public void yield_of_money_prop_inside_expr_is_transformed_into_yields_for_all_components_02() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("pricePerLitre.amount")
                .yield().expr(expr().prop("pricePerLitre.currency").model()).as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_inside_caseWhen_is_transformed_into_yields_for_all_components_01() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().caseWhen().prop("vehicle.key").like().val("c%")
                        .then().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr()
                        .when().prop("vehicle.key").like().val("x%")
                        .then().val(zero)
                        .otherwise().prop("vehicle.price")
                        .end()
                    .as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().caseWhen().prop("vehicle.key").like().val("c%")
                        .then().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr()
                        .when().prop("vehicle.key").like().val("x%")
                        .then().val(zero)
                        .otherwise().prop("vehicle.price")
                        .end()
                    .as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_inside_caseWhen_is_transformed_into_yields_for_all_components_02() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().caseWhen().prop("vehicle.key").like().val("c%")
                        .then().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr()
                        .when().prop("vehicle.key").like().val("x%")
                        .then().val(zero)
                        .otherwise().prop("vehicle.price")
                        .end()
                    .as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().caseWhen().prop("vehicle.key").like().val("c%")
                        .then().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr()
                        .when().prop("vehicle.key").like().val("x%")
                        .then().val(zero)
                        .otherwise().prop("vehicle.price")
                        .end()
                    .as("pricePerLitre.amount")
                .yield().expr(expr().prop("pricePerLitre.currency").model()).as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_inside_sub_query_into_money_prop_without_currency_is_transformed_into_a_yield_for_amount() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre").modelAsPrimitive()).as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("vehicle.id").as("id")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre").modelAsPrimitive()).as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_inside_sub_query_into_money_prop_with_currency_cannot_be_specified_without_yielding_currency() {
        final var invalidQuery1 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre").modelAsPrimitive()).as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        assertThatThrownBy(() -> qry(invalidQuery1))
                .hasMessage("[pricePerLitre.currency] must be yielded into explicitly. [pricePerLitre.currency] could not be inferred from yield [pricePerLitre]: Inference is not supported for sub-queries.");

        final var validQuery1 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre").modelAsPrimitive()).as("pricePerLitre")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre.currency").modelAsPrimitive()).as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("id").as("id")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre").modelAsPrimitive()).as("pricePerLitre.amount")
                .yield().model(select(TgFuelUsage.class).yield().maxOf().prop("pricePerLitre.currency").modelAsPrimitive()).as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(validQuery1));
    }

    @Test
    public void yield_of_money_prop_into_money_prop_without_currency_is_transformed_into_a_yield_for_amount() {
        final var query1 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre").as("price")
                .modelAsEntity(TgVehicle.class);

        final var query2 = select(TgFuelUsage.class)
                .yield().prop("pricePerLitre.amount").as("price.amount")
                .modelAsEntity(TgVehicle.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_of_money_prop_into_money_prop_with_currency_cannot_be_specified_without_yielding_currency() {
        final var invalidQuery1 = select(TgVehicle.class)
                .yield().prop("price").as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        assertThatThrownBy(() -> qry(invalidQuery1))
                .hasMessage("[pricePerLitre.currency] must be yielded into explicitly. [pricePerLitre.currency] could not be inferred from yield [pricePerLitre]: Inference did not produce any results.");

        final var validQuery1 = select(TgVehicle.class)
                .yield().prop("price").as("pricePerLitre")
                .yield().val("USD").as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        final var query2 = select(TgVehicle.class)
                .yield().prop("price").as("pricePerLitre.amount")
                .yield().val("USD").as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        assertEquals(qry(query2), qry(validQuery1));
    }

}
