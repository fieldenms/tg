package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.stage1.sundries.ExpandMoneyTypedPropYield1;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;

/// An operational test for [ExpandMoneyTypedPropYield1].
///
public class ExpandMoneyTypedPropYield1ExecutionTest extends AbstractDaoTestCase {

    private static final String CAR_1 = "CAR1";
    private static final Money CAR_1_price = new Money("200");
    private static final Money FUEL_USAGE_CAR_1_pricePerLitre = new Money("1.25", Currency.getInstance("EUR"));

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    @Test
    public void money_typed_prop_yield_is_transformed_into_yields_for_all_components_01() {
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgVehicle.class, "price").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT);

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
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgFuelUsage.class, "pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);

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
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgVehicle.class, "price").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT);

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
        assertThat(((ICompositeUserTypeInstantiate) domainMetadata.forProperty(TgFuelUsage.class, "pricePerLitre").hibType())
                           .getPropertyNames())
                .containsExactlyInAnyOrder(AMOUNT, CURRENCY);

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
                     .setQty(new BigDecimal("47.5"))
                     .setPricePerLitre(FUEL_USAGE_CAR_1_pricePerLitre)
                     .setFuelType(petrolFuelType));

        save(new_composite(TgMeterReading.class, car1, date("2006-02-09 00:00:00"))
                     .setReading(25)
                     .setFuelUsage(fuelUsage));
    }

}
