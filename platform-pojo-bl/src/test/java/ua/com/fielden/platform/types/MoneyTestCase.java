package ua.com.fielden.platform.types;

import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.types.tuples.T2;

/**
 * A test case for basic operations on type {@link Money}.
 *
 * @author TG Team
 *
 */
public class MoneyTestCase {

    private static final Currency GBR_CURRENCY = Currency.getInstance(Locale.UK);
    private static final Locale ORIG_DEFAULT_LOCALE = Locale.getDefault();
    private static final BinaryOperator<T2<BigDecimal, Money>> NO_PARALLEL_SUPPORT = (a, b) -> {throw new UnsupportedOperationException();};

    @Before
    public void setUp() {
        Locale.setDefault(Locale.UK); // this is needed only to represent GBP as £
    }

    @After
    public void tearDown() {
        Locale.setDefault(ORIG_DEFAULT_LOCALE); // and let's return the original locale not to upset other test cases
    }

    @Test
    public void converstion_to_string_uses_instance_specific_currency_and_2_and_4_fraction_digits() {
        Locale.setDefault(Locale.JAPAN);
        final Currency defaultCurrency = Currency.getInstance(Locale.getDefault());
        assertEquals("￥", defaultCurrency.getSymbol());
        assertEquals("￥1.53", new Money(new BigDecimal("1.53"), Currency.getInstance(Locale.JAPAN)).toString());
        assertEquals("£1.53", new Money(new BigDecimal("1.53"), Currency.getInstance("GBP")).toString());
        assertEquals("A$1,401.53", new Money(new BigDecimal("1401.53"), Currency.getInstance("AUD")).toString());
        assertEquals("A$1,401.5345", new Money(new BigDecimal("1401.5345"), Currency.getInstance("AUD")).toString());
        assertEquals("A$1,401.5345", new Money(new BigDecimal("1401.53451"), Currency.getInstance("AUD")).toString());
        assertEquals("A$1,401.5346", new Money(new BigDecimal("1401.53455"), Currency.getInstance("AUD")).toString());
        assertEquals("A$1,401.5346", new Money(new BigDecimal("1401.53459"), Currency.getInstance("AUD")).toString());
        assertEquals("$1.001", new Money(new BigDecimal("1.001"), Currency.getInstance("USD")).toString());
        assertEquals("NZ$42.00", new Money(new BigDecimal("42.00"), Currency.getInstance("NZD")).toString());
        assertEquals("NZ$42.00", new Money(new BigDecimal("42"), Currency.getInstance("NZD")).toString());
    }

    @Test
    public void plus_operation_for_money_is_equivalent_to_add_operation_for_BigDecimal() {
        final T2<BigDecimal, Money> result = random(1_000).limit(10).map(v -> t2(v, new Money(v, GBR_CURRENCY)))
                                           .reduce(t2(BigDecimal.ZERO, Money.zero), (prev, curr) -> t2(prev._1.add(curr._1), prev._2.plus(curr._2)) , NO_PARALLEL_SUPPORT);
        assertTrue(equalsEx(result._1, result._2.getAmount()));
    }

    @Test
    public void plus_operation_on_a_tax_aware_instance_produces_a_tax_aware_result_where_the_tax_nature_of_the_operand_is_ignored() {
        final Money taxAware = new Money(new BigDecimal("100.00"), 20, GBR_CURRENCY);
        final Money taxUnaware = new Money(new BigDecimal("20.00"), GBR_CURRENCY);
        final Money amount = taxAware.plus(taxUnaware);
        assertEquals(new BigDecimal("120.0000"), amount.getAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("100.0000"), amount.getExTaxAmount());
    }

    @Test
    public void plus_operation_on_a_tax_unaware_instance_with_a_tax_aware_operand_produces_a_tax_unaware_result() {
        final Money taxUnaware = new Money(new BigDecimal("20.00"), GBR_CURRENCY);
        final Money taxAware = new Money(new BigDecimal("100.00"), 20, GBR_CURRENCY);
        final Money amount = taxUnaware.plus(taxAware);
        assertEquals(new BigDecimal("120.0000"), amount.getAmount());
        assertNull(amount.getTaxPercent());
        assertNull(amount.getTaxAmount());
        assertNull(amount.getExTaxAmount());
    }

    @Test
    public void minus_operation_for_money_is_equivalent_to_subtract_operation_for_BigDecimal() {
        final T2<BigDecimal, Money> result = random(100).limit(7).map(v -> t2(v, new Money(v, GBR_CURRENCY)))
                .reduce(t2(new BigDecimal("1000.0000"), new Money(new BigDecimal("1000.0000"), GBR_CURRENCY)), (prev, curr) -> t2(prev._1.subtract(curr._1), prev._2.minus(curr._2)) , NO_PARALLEL_SUPPORT);
        assertTrue(equalsEx(result._1, result._2.getAmount()));
    }

    @Test
    public void minus_operation_on_a_tax_aware_instance_produces_a_tax_aware_result_where_the_tax_nature_of_the_operand_is_ignored() {
        final Money taxAware = new Money(new BigDecimal("100.00"), 20, GBR_CURRENCY);
        final Money taxUnaware = new Money(new BigDecimal("20.00"), GBR_CURRENCY);
        final Money amount = taxAware.minus(taxUnaware);
        assertEquals(new BigDecimal("80.0000"), amount.getAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("13.3333"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("66.6667"), amount.getExTaxAmount());
    }

    @Test
    public void minus_operation_on_a_tax_unaware_instance_with_a_tax_aware_operand_produces_a_tax_unaware_result() {
        final Money taxUnaware = new Money(new BigDecimal("100.00"), GBR_CURRENCY);
        final Money taxAware = new Money(new BigDecimal("20.00"), 10, GBR_CURRENCY);
        final Money amount = taxUnaware.minus(taxAware);
        assertEquals(new BigDecimal("80.0000"), amount.getAmount());
        assertNull(amount.getTaxPercent());
        assertNull(amount.getTaxAmount());
        assertNull(amount.getExTaxAmount());
    }

    @Test
    public void multiply_operation_for_money_is_equivalent_to_multiply_operation_for_BigDecimal_with_HELF_EVEN_rounding() {
        final T2<BigDecimal, Money> result = random(10).limit(7).map(v -> t2(v, new Money(v, GBR_CURRENCY)))
                .reduce(t2(BigDecimal.ONE, Money.ONE), (prev, curr) -> t2(prev._1.multiply(curr._1).setScale(4, HALF_EVEN), prev._2.multiply(curr._1)) , NO_PARALLEL_SUPPORT);
        assertTrue(equalsEx(result._1, result._2.getAmount()));
    }

    @Test
    public void multiply_operation_on_a_tax_aware_instance_produces_a_tax_aware_result() {
        final Money taxAware = new Money(new BigDecimal("100.00"), 20, GBR_CURRENCY);
        final BigDecimal value = new BigDecimal("10.00");

        final Money amount = taxAware.multiply(value);
        assertEquals(new BigDecimal("1000.0000"), amount.getAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("166.6667"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("833.3333"), amount.getExTaxAmount());
    }

    @Test
    public void divide_operation_for_money_is_equivalent_to_divide_operation_for_BigDecimal_with_HELF_EVEN_rounding() {
        final T2<BigDecimal, Money> result = random(5).limit(7).map(v -> equalsEx(BigDecimal.ZERO, v) ? t2(BigDecimal.ONE, new Money(BigDecimal.ONE, GBR_CURRENCY)) : t2(v, new Money(v, GBR_CURRENCY)))
                .reduce(t2(new BigDecimal("10000.0000"), new Money(new BigDecimal("10000.00"))), (prev, curr) -> t2(prev._1.divide(curr._1, HALF_EVEN), prev._2.divide(curr._1)) , NO_PARALLEL_SUPPORT);
        assertTrue(equalsEx(result._1, result._2.getAmount()));
    }

    @Test
    public void divide_operation_on_a_tax_aware_instance_produces_a_tax_aware_result() {
        final Money taxAware = new Money(new BigDecimal("97.00"), 20, GBR_CURRENCY);
        final BigDecimal value = new BigDecimal("10.00");

        final Money amount = taxAware.divide(value);
        assertEquals(new BigDecimal("9.7000"), amount.getAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("1.6167"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("8.0833"), amount.getExTaxAmount());
    }

    @Test
    public void split_operation_distributes_a_monetary_value_into_parts_that_can_be_summed_up_producing_the_original_value() {
        final Money amount = new Money(new BigDecimal("100"), GBR_CURRENCY);
        final List<Money> parts = amount.split(3);
        assertEquals(3, parts.size());
        assertEquals(amount, parts.get(0).plus(parts.get(1)).plus(parts.get(2)));

        // let's also check the parts that the amount was split into
        // it is expected that there would be 2 parts with the value of 33.3333 and one part with the value 33.3334 (the lucky part)
        // the position of the lucky part during splitting is allocated at random, hence the need counting rather than direct comparison of parts
        int numberOfEvenParts = 0, numberOfLuckyParts = 0;
        final BigDecimal eventPartValue = new BigDecimal("33.3333"), luckyPartValue = new BigDecimal("33.3334");
        for (int i = 0; i < 3; i++) {
            if (parts.get(i).getAmount().equals(eventPartValue)) {
                numberOfEvenParts++;
            } else if (parts.get(i).getAmount().equals(luckyPartValue)) {
                numberOfLuckyParts++;
            }
        }

        assertEquals(1, numberOfLuckyParts);
        assertEquals(2, numberOfEvenParts);
        

    }

    @Test
    public void split_operation_of_a_tax_aware_amount_produces_tax_aware_amounts() {
        final Integer taxPercentage = Integer.valueOf("20");
        final Money taxAware = new Money(new BigDecimal("100"), taxPercentage, GBR_CURRENCY);

        final List<Money> parts = taxAware.split(3);
        assertEquals(3, parts.size());
        final Money summedParts = parts.get(0).plus(parts.get(1)).plus(parts.get(2));
        assertEquals(taxAware, summedParts);
        assertEquals(taxAware.getTaxPercent(), summedParts.getTaxPercent());
        assertEquals(taxAware.getTaxAmount(), summedParts.getTaxAmount());
        assertEquals(taxAware.getExTaxAmount(), summedParts.getExTaxAmount());
        
        // assert that each part is also tax aware
        final BigDecimal partTaxAmount = new BigDecimal("5.5556");
        for (int i = 0; i < 3; i++) {
            assertEquals("Incorrect tax percentage.", taxPercentage, parts.get(i).getTaxPercent());
            assertEquals("Incorrect tax amount.", partTaxAmount, parts.get(i).getTaxAmount());
        }
    }

    @Test
    public void tax_aware_money_can_be_instantiated_by_specifying_tax_percentage() {
        // test tax calculations
        final Money m = new Money(new BigDecimal("120.00"), 20, GBR_CURRENCY);
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), m.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("100.0000"), m.getExTaxAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("20"), m.getTaxPercent());
        // test tax validation
        try {
            new Money(new BigDecimal("100.00"), 0, GBR_CURRENCY);
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }
        try {
            new Money(new BigDecimal("100.00"), -1, GBR_CURRENCY);
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }

        try {
            new Money(new BigDecimal("100.00"), 101, GBR_CURRENCY);
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void tax_aware_money_can_be_instantiated_by_specifying_tax_amount() {
        // test tax calculations
        final Money m = new Money(new BigDecimal("145.00"), new BigDecimal("20.00"), GBR_CURRENCY);
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), m.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("125.0000"), m.getExTaxAmount());
        assertEquals("Incorrect tax percentage.", Integer.valueOf("16"), m.getTaxPercent());
        // test tax validation
        try {
            new Money(new BigDecimal("100.00"), new BigDecimal("1000.00"), GBR_CURRENCY);
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }
        try {
            new Money(new BigDecimal("100.00"), null, GBR_CURRENCY);
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }

        try {
            new Money(new BigDecimal("100.00"), new BigDecimal("-10.00"), GBR_CURRENCY);
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void comparison_operations_for_Money_are_equivalent_to_comparions_operations_for_BigDecimal() {
        assertTrue("Incorrect result of lt.", new Money("2.00").lt(new Money("2.01")));
        assertTrue("Incorrect result of le.", new Money("2.00").le(new Money("2.00")));
        assertTrue("Incorrect result of eq.", new Money("2.00").eq(new Money("2.00")));
        assertTrue("Incorrect result of eq.", new Money("2.0000").eq(new Money("2.00")));
        assertTrue("Incorrect result of gt.", new Money("2.00").gt(new Money("1.99")));
        assertTrue("Incorrect result of ge.", new Money("2.00").ge(new Money("2.00")));
    }

    /**
     * A helper function to generate an infinite stream of random decimals between {@code zero} and {@code max}, and a random scale between 0 and 4.
     *
     * @param max
     * @return
     */
    private static Stream<BigDecimal> random(final int max) {
        return Stream.generate(() -> new BigDecimal(ThreadLocalRandom.current().nextDouble(max + 1)).setScale(ThreadLocalRandom.current().nextInt(0, 5), RoundingMode.HALF_EVEN));
    }

}