package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TeProductPriceWithCurrency;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/// This test covers the use of [Money] with currency as a key member in operations, such as entity saving and fetching.
///
public class MoneyWithCurrencyAsKeyMemberOperationsTest extends AbstractDaoTestCase {

    @Test
    public void two_entities_with_all_key_member_values_equal_except_currency_are_saved_as_different_records() {
        final var newEntity1 = new_(TeProductPriceWithCurrency.class).setProduct("Wrench").setPrice(new Money("5", Currency.getInstance("USD")));
        final var newEntity2 = new_(TeProductPriceWithCurrency.class).setProduct("Wrench").setPrice(new Money("5", Currency.getInstance("EUR")));
        assertNotEquals(newEntity1.getKey(), newEntity2.getKey());
        assertEquals(newEntity1.getProduct(), newEntity2.getProduct());
        assertNotEquals(newEntity1.getPrice(), newEntity2.getPrice());
        assertEquals(newEntity1.getPrice().getAmount(), newEntity2.getPrice().getAmount());
        assertNotEquals(newEntity1.getPrice().getCurrency(), newEntity2.getPrice().getCurrency());

        final var savedEntity1 = save(newEntity1);
        final var savedEntity2 = save(newEntity2);
        // The fact of saving the second instance is enough, but let's ensure IDs are different for completeness.
        assertNotEquals(savedEntity1.getId(), savedEntity2.getId());
        assertNotEquals(savedEntity1.getKey(), savedEntity2.getKey());
        assertEquals(savedEntity1.getProduct(), savedEntity2.getProduct());
        assertNotEquals(savedEntity1.getPrice(), savedEntity2.getPrice());
        assertEquals(savedEntity1.getPrice().getAmount(), savedEntity2.getPrice().getAmount());
        assertNotEquals(savedEntity1.getPrice().getCurrency(), savedEntity2.getPrice().getCurrency());
    }

    @Test
    public void entityWithKeyExists_is_true_if_both_amount_and_currency_match() {
        save(new_(TeProductPriceWithCurrency.class).setProduct("Wrench").setPrice(new Money("5", Currency.getInstance("USD"))));
        assertTrue(co(TeProductPriceWithCurrency.class).entityWithKeyExists("Wrench", new Money("5", Currency.getInstance("USD"))));
        assertFalse(co(TeProductPriceWithCurrency.class).entityWithKeyExists("Wrench", new Money("5", Currency.getInstance("EUR"))));
        assertFalse(co(TeProductPriceWithCurrency.class).entityWithKeyExists("Wrench", new Money("5.5", Currency.getInstance("USD"))));
        assertFalse(co(TeProductPriceWithCurrency.class).entityWithKeyExists("Wrenches", new Money("5", Currency.getInstance("USD"))));
    }

    @Test
    public void findByKey_does_find_if_both_amount_and_currency_match() {
        save(new_(TeProductPriceWithCurrency.class).setProduct("Wrench").setPrice(new Money("5", Currency.getInstance("USD"))));
        assertThat(co(TeProductPriceWithCurrency.class).findByKeyOptional("Wrench", new Money("5", Currency.getInstance("USD")))).isPresent();
        assertThat(co(TeProductPriceWithCurrency.class).findByKeyOptional("Wrench", new Money("5", Currency.getInstance("EUR")))).isEmpty();
        assertThat(co(TeProductPriceWithCurrency.class).findByKeyOptional("Wrench", new Money("5.5", Currency.getInstance("USD")))).isEmpty();
        assertThat(co(TeProductPriceWithCurrency.class).findByKeyOptional("Wrenches", new Money("5", Currency.getInstance("USD")))).isEmpty();
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
    }

}
