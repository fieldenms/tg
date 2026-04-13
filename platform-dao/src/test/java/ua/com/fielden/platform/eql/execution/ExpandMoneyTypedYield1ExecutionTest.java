package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.stage1.sundries.ExpandMoneyTypedYield1;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static graphql.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;

/// An operational test for [ExpandMoneyTypedYield1].
///
public class ExpandMoneyTypedYield1ExecutionTest extends AbstractDaoTestCase {

    private static final String CAR_1 = "CAR1";
    private static final Money CAR_1_price = new Money("200");
    private static final Money FUEL_USAGE_CAR_1_pricePerLitre = new Money("1.25", Currency.getInstance("EUR"));
    private static final BigDecimal FUEL_USAGE_CAR_1_qty = new BigDecimal("5");

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    @Test
    public void test_entities_have_expected_Hibernate_types() {
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgVehicle.class, "price").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT);
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgFuelUsage.class, "pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgMeterReading.class, "fuelUsage.pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);
    }

    @Test
    public void money_typed_prop_yield_is_transformed_into_yields_for_all_components_01() {
        final var query = select(TgVehicle.class)
                .where().prop(KEY).eq().val(CAR_1)
                .yield().prop(ID).as(ID)
                .yield().prop(KEY).as(KEY)
                .yield().prop("price").as("price")
                .modelAsEntity(TgVehicle.class);

        final var vehicle = co(TgVehicle.class).getEntity(from(query).model());
        assertThat(vehicle.getPrice())
                .isNotNull()
                .isEqualTo(CAR_1_price);
    }

    @Test
    public void money_typed_prop_yield_is_transformed_into_yields_for_all_components_02() {
        final var query = select(TgFuelUsage.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop(ID).as(ID)
                .yield().prop(KEY).as(KEY)
                .yield().prop("pricePerLitre").as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var fuelUsage = co(TgFuelUsage.class).getEntity(from(query).model());
        assertThat(fuelUsage.getPricePerLitre())
                .isNotNull()
                .isEqualTo(FUEL_USAGE_CAR_1_pricePerLitre);
    }

    @Test
    public void money_typed_prop_path_yield_is_transformed_into_yields_for_all_components_01() {
        final var query = select(TgFuelUsage.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop("vehicle.id").as(ID)
                .yield().prop("vehicle.key").as(KEY)
                .yield().prop("vehicle.price").as("price")
                .modelAsEntity(TgVehicle.class);

        final var fuelUsage = co(TgVehicle.class).getEntity(from(query).model());
        assertThat(fuelUsage.getPrice())
                .isNotNull()
                .isEqualTo(CAR_1_price);
    }

    @Test
    public void money_typed_prop_path_yield_is_transformed_into_yields_for_all_components_02() {
        final var query = select(TgMeterReading.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop("fuelUsage.id").as(ID)
                .yield().prop("fuelUsage.key").as(KEY)
                .yield().prop("fuelUsage.pricePerLitre").as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var fuelUsage = co(TgFuelUsage.class).getEntity(from(query).model());
        assertThat(fuelUsage.getPricePerLitre())
                .isNotNull()
                .isEqualTo(FUEL_USAGE_CAR_1_pricePerLitre);
    }

    @Test
    public void alias_of_a_yield_of_expr_into_a_Money_typed_property_is_transformed_into_an_alias_for_amount_01() {
        final var query = select(TgFuelUsage.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop("vehicle.id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("price")
                .modelAsEntity(TgVehicle.class);

        final var vehicle = co(TgVehicle.class).getEntity(from(query).model());
        assertNotNull(vehicle.getPrice());
        assertEquals(FUEL_USAGE_CAR_1_pricePerLitre.multiply(FUEL_USAGE_CAR_1_qty).getAmount(), vehicle.getPrice().getAmount());
    }

    @Test
    public void alias_of_a_yield_of_expr_into_a_Money_typed_property_is_transformed_into_an_alias_for_amount_02() {
        final var query = select(TgFuelUsage.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop("id").as("id")
                .yield().beginExpr().prop("pricePerLitre").mult().prop("qty").endExpr().as("pricePerLitre")
                .modelAsEntity(TgFuelUsage.class);

        final var fuelUsage = co(TgFuelUsage.class).getEntity(from(query).model());
        assertNotNull(fuelUsage.getPricePerLitre());
        assertEquals(FUEL_USAGE_CAR_1_pricePerLitre.multiply(FUEL_USAGE_CAR_1_qty).getAmount(), fuelUsage.getPricePerLitre().getAmount());
    }

    @Test
    public void yield_money_typed_prop_with_amount_and_currency_into_a_money_typed_prop_with_amount() {
        final var query = select(TgFuelUsage.class)
                .where().prop("vehicle.key").eq().val(CAR_1)
                .yield().prop("vehicle.id").as(ID)
                .yield().prop("vehicle.key").as(KEY)
                .yield().prop("pricePerLitre").as("price")
                .modelAsEntity(TgVehicle.class);

        final var vehicle = co(TgVehicle.class).getEntity(from(query).model());
        assertNotNull(vehicle.getPrice());
        assertEquals(FUEL_USAGE_CAR_1_pricePerLitre.getAmount(), vehicle.getPrice().getAmount());
    }

    @Test
    public void yield_money_and_currency_explicitly() {
        final var query = select(TgFuelUsage.class)
                .yield().prop("id").as(ID)
                .yield().prop("key").as(KEY)
                .yield().prop("pricePerLitre").as("pricePerLitre")
                .yield().val("JPY").as("pricePerLitre.currency")
                .modelAsEntity(TgFuelUsage.class);

        final var vehicle = co(TgFuelUsage.class).getEntity(from(query).model());
        assertNotNull(vehicle.getPricePerLitre());
        assertEquals(FUEL_USAGE_CAR_1_pricePerLitre.getAmount(), vehicle.getPricePerLitre().getAmount());
        assertNotEquals(Currency.getInstance("JPY"), FUEL_USAGE_CAR_1_pricePerLitre.getCurrency());
        assertEquals(Currency.getInstance("JPY"), vehicle.getPricePerLitre().getCurrency());
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

        final var petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final var merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));

        final var m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));

        final var car1 = save(new_(TgVehicle.class, CAR_1, "Car 1").setInitDate(date("2001-01-01 00:00:00"))
                                      .setModel(m316)
                                      .setPrice(CAR_1_price)
                                      .setPurchasePrice(new Money("199"))
                                      .setActive(true));

        final var fuelUsage = save(new_composite(TgFuelUsage.class, car1, date("2006-02-09 00:00:00"))
                     .setQty(FUEL_USAGE_CAR_1_qty)
                     .setPricePerLitre(FUEL_USAGE_CAR_1_pricePerLitre)
                     .setFuelType(petrolFuelType));

        save(new_composite(TgMeterReading.class, car1, date("2006-02-09 00:00:00"))
                     .setReading(25)
                     .setFuelUsage(fuelUsage));
    }

}
