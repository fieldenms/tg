package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.AbstractDependentMoneyCurrencyHandler;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.GenericTgFuelUsagePricePerLitreCurrencyHandler;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import java.util.Currency;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.sample.domain.GenericTgFuelUsagePricePerLitreCurrencyHandler.ERR_CANNOT_DETERMINE_FROM_LOCATION;

/// A test case that covers [GenericTgFuelUsagePricePerLitreCurrencyHandler] which is based on [AbstractDependentMoneyCurrencyHandler].
///
public class TgFuelUsagePricePerLitreCurrencyTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void pricePerLitre_is_invalid_if_location_is_not_mapped_to_a_currency() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Denmark");

        entity.setPricePerLitre(new Money("10"));
        assertFalse(entity.getProperty("pricePerLitre").isValid());
        assertEquals(ERR_CANNOT_DETERMINE_FROM_LOCATION.formatted("Denmark"),
                     entity.getProperty("pricePerLitre").getFirstFailure().getMessage());
    }

    @Test
    public void pricePerLitre_becomes_valid_once_a_location_with_a_mapped_currency_is_assigned() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Denmark");

        entity.setPricePerLitre(new Money("10"));
        assertFalse(entity.getProperty("pricePerLitre").isValid());
        assertEquals(ERR_CANNOT_DETERMINE_FROM_LOCATION.formatted("Denmark"),
                     entity.getProperty("pricePerLitre").getFirstFailure().getMessage());

        entity.setLocation("Australia");
        assertTrue(entity.getProperty("pricePerLitre").isValid());
    }

    @Test
    public void pricePerLitre_currency_changes_after_location_changes() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Denmark");

        entity.setPricePerLitre(new Money("10", Currency.getInstance("JPY")));
        assertFalse(entity.getProperty("pricePerLitre").isValid());
        assertEquals(ERR_CANNOT_DETERMINE_FROM_LOCATION.formatted("Denmark"),
                     entity.getProperty("pricePerLitre").getFirstFailure().getMessage());

        entity.setLocation("Australia");
        assertTrue(entity.getProperty("pricePerLitre").isValid());
        assertEquals(new Money("10", Currency.getInstance("AUD")), entity.getPricePerLitre());

        entity.setLocation("Ukraine");
        assertTrue(entity.getProperty("pricePerLitre").isValid());
        assertEquals(new Money("10", Currency.getInstance("UAH")), entity.getPricePerLitre());
    }

    @Test
    public void if_location_is_assigned_then_first_assignment_of_pricePerLitre_will_use_currency_from_location() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Iceland");

        assertNull(entity.getPricePerLitre());
        entity.setPricePerLitre(new Money("10", Currency.getInstance("JPY")));
        assertTrue(entity.getProperty("pricePerLitre").isValid());
        assertEquals(new Money("10", Currency.getInstance("ISK")), entity.getPricePerLitre());
    }

    @Test
    public void if_location_with_a_mapped_currency_is_changed_to_one_without_a_mapped_currency_then_assigned_pricePerLitre_becomes_invalid() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Australia");
        entity.setPricePerLitre(new Money("10"));
        assertEquals(new Money("10", Currency.getInstance("AUD")), entity.getPricePerLitre());

        entity.setLocation("Denmark");
        assertFalse(entity.getProperty("pricePerLitre").isValid());
        assertEquals(ERR_CANNOT_DETERMINE_FROM_LOCATION.formatted("Denmark"),
                     entity.getProperty("pricePerLitre").getFirstFailure().getMessage());
    }

    @Test
    public void assigned_pricePerLitre_can_be_reset_to_null() {
        final var entity = factory.newEntity(TgFuelUsage.class);
        entity.setLocation("Australia");
        entity.setPricePerLitre(new Money("10"));
        assertEquals(new Money("10", Currency.getInstance("AUD")), entity.getPricePerLitre());

        entity.setPricePerLitre(null);
        assertTrue(entity.getProperty("pricePerLitre").isValid());
    }

}
