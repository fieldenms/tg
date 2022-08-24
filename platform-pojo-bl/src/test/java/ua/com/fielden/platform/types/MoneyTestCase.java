package ua.com.fielden.platform.types;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

public class MoneyTestCase extends TestCase {

    static {
        // Deliberately set Locale with a currency symbol other than $ or ₴, to be sure that the test will work reliably in both AU and UA.
        Locale.setDefault(Locale.UK);
    }

    public void testPlus() {
        // ---------------------------------------------------------
        Money m1 = new Money(new BigDecimal("1.53"), Currency.getInstance("GBP")), m2 = new Money(new BigDecimal("3.41"), Currency.getInstance("GBP"));

        Money m = m1.plus(m2);
        assertEquals("£4.94", m.toString());

        // ---------------------------------------------------------
        m1 = new Money(new BigDecimal("100.53"), Currency.getInstance("GBP"));
        m2 = new Money(new BigDecimal("300.41"), Currency.getInstance("GBP"));

        m = m1.plus(m2);
        assertEquals("£400.94", m.toString());
    }

    public void testTaxSensitivePlus() {
        final Money m = new Money(new BigDecimal("100.00"), 20, Currency.getInstance("GBP"));
        final Money amount = m.plus(new Money(new BigDecimal("20.00"), Currency.getInstance("GBP")));
        assertEquals(new BigDecimal("120.0000"), amount.getAmount());
        assertEquals("Should be tax sensitive.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("100.0000"), amount.getExTaxAmount());
    }

    public void testMinus() {
        // ---------------------------------------------------------
        Money m1 = new Money(new BigDecimal("1.53"), Currency.getInstance("GBP")), m2 = new Money(new BigDecimal("3.41"), Currency.getInstance("GBP"));

        Money m = m2.minus(m1);
        assertEquals("£1.88", m.toString());

        // ---------------------------------------------------------
        m1 = new Money(new BigDecimal("1.53"), Currency.getInstance("GBP"));
        m2 = new Money(new BigDecimal("3.41"), Currency.getInstance("GBP"));

        m = m2.minus(m1);
        assertEquals("£1.88", m.toString());
    }

    public void testTaxSensitiveMinus() {
        final Money m = new Money(new BigDecimal("100.00"), 20, Currency.getInstance("GBP"));
        final Money amount = m.minus(new Money(new BigDecimal("20.00"), Currency.getInstance("GBP")));
        assertEquals(new BigDecimal("80.0000"), amount.getAmount());
        assertEquals("Should be tax sensitive.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("13.3333"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("66.6667"), amount.getExTaxAmount());
    }

    public void testMultiply() {
        // ---------------------------------------------------------
        Money m = new Money(new BigDecimal("4.89"), Currency.getInstance("GBP"));
        final BigDecimal value = new BigDecimal("0.35");

        m = m.multiply(value);
        assertEquals("£1.7115", m.toString());
        // ---------------------------------------------------------
        m = new Money(new BigDecimal("400.89"), Currency.getInstance("GBP"));

        m = m.multiply(value);
        assertEquals("£140.3115", m.toString());
    }

    public void testTaxSensitiveMultiply() {
        final Money m = new Money(new BigDecimal("100.00"), 20, Currency.getInstance("GBP"));
        final BigDecimal value = new BigDecimal("10.00");

        final Money amount = m.multiply(value);
        assertEquals(new BigDecimal("1000.0000"), amount.getAmount());
        assertEquals("Should be tax sensitive.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("166.6667"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("833.3333"), amount.getExTaxAmount());
    }

    public void testDivide() {
        // ---------------------------------------------------------
        Money m = new Money(new BigDecimal("4.89"), Currency.getInstance("GBP"));
        final BigDecimal value = new BigDecimal("1.37");

        m = m.divide(value);
        assertEquals("£3.5693", m.toString());

        // ---------------------------------------------------------
        m = new Money(new BigDecimal("400.89"), Currency.getInstance("GBP"));

        m = m.divide(value);
        assertEquals("£292.6204", m.toString());
    }

    public void testTaxSensitiveDivide() {
        final Money m = new Money(new BigDecimal("97.00"), 20, Currency.getInstance("GBP"));
        final BigDecimal value = new BigDecimal("10.00");

        final Money amount = m.divide(value);
        assertEquals(new BigDecimal("9.7000"), amount.getAmount());
        assertEquals("Should be tax sensitive.", Integer.valueOf("20"), amount.getTaxPercent());
        assertEquals("Incorrect tax amount.", new BigDecimal("1.6167"), amount.getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("8.0833"), amount.getExTaxAmount());
    }

    public void testSplit() {
        final Money m = new Money(new BigDecimal(100d), Currency.getInstance("GBP"));

        final List<Money> parts = m.split(3);
        assertEquals(3, parts.size());

        int numberOfSingleParts = 0, numberOfLuckyParts = 0;
        final String singlePart = "£33.3333", luckyPart = "£33.3334";
        for (int i = 0; i < 3; i++) {
            if (parts.get(i).toString().equals(singlePart)) {
                numberOfSingleParts++;
            } else if (parts.get(i).toString().equals(luckyPart)) {
                numberOfLuckyParts++;
            }
        }

        assertEquals(1, numberOfLuckyParts);
        assertEquals(2, numberOfSingleParts);
    }

    public void testSplitForTaxSensitiveAmount() {
        final Money m = new Money(new BigDecimal(100d), 20, Currency.getInstance("GBP"));

        final List<Money> parts = m.split(3);
        assertEquals(3, parts.size());

        int numberOfSingleParts = 0, numberOfLuckyParts = 0;
        final String singlePart = "£33.3333", luckyPart = "£33.3334";
        for (int i = 0; i < 3; i++) {
            assertEquals("This part should be tax sensitive.", Integer.valueOf("20"), parts.get(i).getTaxPercent());
            assertEquals("Incorrect tax amount.", new BigDecimal("5.5556"), parts.get(i).getTaxAmount());

            if (parts.get(i).toString().equals(singlePart)) {
                numberOfSingleParts++;
            } else if (parts.get(i).toString().equals(luckyPart)) {
                numberOfLuckyParts++;
            }
        }

        assertEquals(1, numberOfLuckyParts);
        assertEquals(2, numberOfSingleParts);
    }

    public void testTaxSensitiveInstantiationBasedOnTaxPercent() {
        // test tax calculations
        final Money m = new Money(new BigDecimal("120.00"), 20, Currency.getInstance("GBP"));
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), m.getTaxAmount());
        assertEquals("Incorrect ex-tax amount", new BigDecimal("100.0000"), m.getExTaxAmount());
        assertEquals("Incorrect tax percent", Integer.valueOf("20"), m.getTaxPercent());
        // test tax validation
        try {
            new Money(new BigDecimal("100.00"), 0, Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }
        try {
            new Money(new BigDecimal("100.00"), -1, Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }

        try {
            new Money(new BigDecimal("100.00"), 101, Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax percent.");
        } catch (final Exception ex) {
        }
    }

    public void testTaxSensitiveInstantiationBasedOnTaxAmount() {
        // test tax calculations
        final Money m = new Money(new BigDecimal("145.00"), new BigDecimal("20.00"), Currency.getInstance("GBP"));
        assertEquals("Incorrect tax amount.", new BigDecimal("20.0000"), m.getTaxAmount());
        assertEquals("Incorrect ex-tax amount", new BigDecimal("125.0000"), m.getExTaxAmount());
        assertEquals("Incorrect tax percent", Integer.valueOf("16"), m.getTaxPercent());
        // test tax validation
        try {
            new Money(new BigDecimal("100.00"), new BigDecimal("1000.00"), Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }
        try {
            new Money(new BigDecimal("100.00"), null, Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }

        try {
            new Money(new BigDecimal("100.00"), new BigDecimal("-10.00"), Currency.getInstance("GBP"));
            fail("Created an instance with invalid tax amount.");
        } catch (final Exception ex) {
        }
    }

    public void testComparisonMethods() {
        assertTrue("Incorrect result of lt.", new Money("2.00").lt(new Money("2.01")));
        assertTrue("Incorrect result of le.", new Money("2.00").le(new Money("2.00")));
        assertTrue("Incorrect result of eq.", new Money("2.00").eq(new Money("2.00")));
        assertTrue("Incorrect result of gt.", new Money("2.00").gt(new Money("1.99")));
        assertTrue("Incorrect result of ge.", new Money("2.00").ge(new Money("2.00")));
    }
}
