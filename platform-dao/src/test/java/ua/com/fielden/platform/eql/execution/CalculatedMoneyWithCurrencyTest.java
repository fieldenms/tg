package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.StreamUtils;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

public class CalculatedMoneyWithCurrencyTest extends AbstractDaoTestCase {

    private static final String
            CAR_1 = "CAR1",
            P = "P";

    @Test
    public void TgFuelUsage_previousPricePerLitre() {
        final var car1 = EntityUtils.<TgVehicle>fetchEntityForPropOf("vehicle", co(TgFuelUsage.class), CAR_1).orElseThrow();
        final var petrol = EntityUtils.<TgFuelType>fetchEntityForPropOf("fuelType", co(TgFuelUsage.class), P).orElseThrow();

        save(new_composite(TgFuelUsage.class, car1, date("2019-02-09 00:00:00"))
                     .setPricePerLitre(new Money("28.6", Currency.getInstance("UAH")))
                     .setQty(new BigDecimal("35"))
                     .setFuelType(petrol));
        save(new_composite(TgFuelUsage.class, car1, date("2024-09-01 00:00:00"))
                     .setPricePerLitre(new Money("56.6", Currency.getInstance("UAH")))
                     .setQty(new BigDecimal("40"))
                     .setFuelType(petrol));
        // Also save records with null price to test that calculated price will also be null.
        save(new_composite(TgFuelUsage.class, car1, date("2024-10-01 00:00:00"))
                     .setPricePerLitre(null)
                     .setQty(new BigDecimal("40"))
                     .setFuelType(petrol));
        save(new_composite(TgFuelUsage.class, car1, date("2024-11-01 00:00:00"))
                     .setPricePerLitre(null)
                     .setQty(new BigDecimal("40"))
                     .setFuelType(petrol));

        final var entities = co(TgFuelUsage.class).getAllEntities(
                from(select(TgFuelUsage.class).where()
                             .prop("vehicle.key").eq().val(CAR_1)
                             .orderBy().prop("date").asc()
                             .model())
                .with(fetchKeyAndDescOnly(TgFuelUsage.class).with("pricePerLitre", "previousPricePerLitre"))
                .model());
        assertThat(entities).hasSizeGreaterThan(1);
        StreamUtils.windowed(entities.stream(), 2)
                .forEach(win -> assertEquals(win.getFirst().getPricePerLitre(), win.get(1).getPreviousPricePerLitre()));
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

        final var merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final var m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));

        final var car1 = save(new_(TgVehicle.class, CAR_1, "Car 1")
                                      .setInitDate(date("2007-01-01 00:00:00"))
                                      .setModel(m316)
                                      .setPrice(new Money("200"))
                                      .setActive(true));

        final var petrolFuelType = save(new_(TgFuelType.class, P, "Petrol"));
    }

}
