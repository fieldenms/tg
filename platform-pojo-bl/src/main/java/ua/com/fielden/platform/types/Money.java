package ua.com.fielden.platform.types;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Collections.unmodifiableList;
import static java.util.Currency.getInstance;
import static java.util.Locale.getDefault;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Random;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;

/**
 * Immutable class representing amount of money of particular currency. Instances of this class could be compared only if their currencies match.
 * <p>
 * <p>
 * All monetary arithmetic operations are performed with 4 decimal places using using {@link RoundingMode#HALF_EVEN} rounding rule. The toString() method rounds up the amount to 2
 * decimal places purely for display only.
 * <p>
 * <p>
 * Supports tax operations. Property <code>amount</code> can represent either a tax inclusive or exclusive amount if used in non-tax sensitive situations. However, when used in a
 * tax-sensitive context then it should at all times represent a full amount (i.e. tax inclusive). Properties <code>exTaxAmount</code> and <code>taxAmount</code> correspond to
 * amount without tax (e.g. without GST) and the tax amount (e.g. GST value) respectively. If this class used in a non-tax sensitive context then properties
 * <code>exTaxAmount</code> and <code>taxAmount</code> are null. However, if class is used in tax-sensitive context, then it is assumed that :
 * <code>amount = (taxPercent/100)*exTaxAmount + exTaxAmount</code>.
 * <p>
 * <p>
 * There are several constructors that can be conveniently used for creation of tax and non-tax sensitive instances. The current rule implies that
 * <code>tax</tax> can always be expressed as integer values representing a percent value.
 * <p><p>
 * <b>IMPORTANT:</b><br/>
 *  1. Currently all monetary arithmetic operations produce tax sensitive instances if the instance operated on is tax sensitive.<br/>
 *  2. Methods equals() and compareTo() use only properties <code>amount</code> and <code>currency</code>.<br/>
 *
 * @author TG Team
 */
public class Money implements Comparable<Money> {

    private static final Random random = new Random();

    /**
     * This is a convenient unit element for addition.
     */
    public static final Money zero = new Money("0.00");

    /**
     * This is a convenient unit element for multiplication.
     */
    public static final Money ONE = new Money("1.00");

    @IsProperty(precision = 18, scale = 2)
    @MapTo
    private final BigDecimal amount;
    
    @IsProperty(precision = 18, scale = 2)
    @MapTo
    private final BigDecimal exTaxAmount;
    
    @IsProperty(precision = 18, scale = 2)
    @MapTo
    private final BigDecimal taxAmount;
    
    @IsProperty
    @MapTo
    private final Integer taxPercent;
    
    @IsProperty
    @MapTo
    private final Currency currency;

    /**
     * Creates an instance not requiring tax data, where null values are assigned to properties <code>exTaxAmount</code> and <code>taxAmount</code>. Constructor parameters should
     * not be null.
     *
     * @param amount
     *            -- monetary amount, which could be either tax inclusive or exclusive -- this depends on the usage context.
     * @param currency
     */
    public Money(final BigDecimal amount, final Currency currency) {
        this.amount = checkParameters(amount, currency);
        exTaxAmount = null;
        taxAmount = null;
        taxPercent = null;
        this.currency = currency;
    }

    /**
     * Creates an instance requiring tax data based on tax percent. Constructor parameters should not be null.
     *
     * @param amount
     *            -- monetary amount tax inclusive
     * @param taxPercent
     *            -- tax percent used for calculation of the tax amount and ex-tax amount
     * @param currency
     */
    public Money(final BigDecimal amount, final int taxPercent, final Currency currency) {
        this.amount = checkParameters(amount, taxPercent, currency);
        this.taxPercent = taxPercent;
        final BigDecimal taxFrac = new BigDecimal(taxPercent / 100d, new MathContext(4));
        exTaxAmount = new BigDecimal(1d, new MathContext(50)).divide(taxFrac.add(BigDecimal.ONE), new MathContext(50, HALF_EVEN)).multiply(this.amount).setScale(4, HALF_EVEN);
        taxAmount = amount.subtract(exTaxAmount).setScale(4, HALF_EVEN);

        this.currency = currency;
    }

    /**
     * Convenience constructor accepting amount as string.
     *
     * @param amount
     * @param taxPercent
     * @param currency
     */
    public Money(final String amount, final int taxPercent, final Currency currency) {
        this(new BigDecimal(amount), taxPercent, currency);
    }

    /**
     * Creates an instance requiring tax data based on tax amount. Constructor parameters should not be null.
     *
     * @param amount
     *            -- monetary amount tax inclusive
     * @param taxAmount
     *            -- tax amount, which is a tax portion of the provided amount; used for calculation of tax percent and ex-tax amount
     * @param currency
     */
    public Money(final BigDecimal amount, final BigDecimal taxAmount, final Currency currency) {
        this.amount = checkParameters(amount, taxAmount, currency);
        this.taxAmount = taxAmount.setScale(4, HALF_EVEN);
        this.taxPercent = taxAmount.multiply(BigDecimal.valueOf(100.0000d)).divide(this.amount.subtract(taxAmount), HALF_UP).setScale(0, HALF_EVEN).intValue();
        exTaxAmount = getAmount().subtract(taxAmount).setScale(4, HALF_EVEN);

        this.currency = currency;
    }

    /**
     * This is a convenience constructor, which utilises a default locale to derive the currency. Creates an instance not requiring tax data, where null values are assigned to
     * properties <code>exTaxAmount</code> and <code>taxAmount</code>.
     *
     * @param amount
     */
    public Money(final BigDecimal amount) {
        this(amount, getInstance(getDefault()));
    }

    /**
     * This is a convenience constructor that accepts string representation of the amount. Creates an instance not requiring tax data, where null values are assigned to properties
     * <code>exTaxAmount</code> and <code>taxAmount</code>.
     *
     * @param amount
     */
    public Money(final String amount, final Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    /**
     * This is a convenience constructor that accepts string representation of the amount and utilises a default locale to derive the currency. Creates an instance not requiring
     * tax data, where null values are assigned to properties <code>exTaxAmount</code> and <code>taxAmount</code>.
     *
     * @param amount
     */
    public Money(final String amount) {
        this(new BigDecimal(amount), Currency.getInstance(getDefault()));
    }
    
    public static Money of(final String amount) {
        return new Money(amount);
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * If currencies match adds two amounts and returns result as new instance. Otherwise throws {@link IllegalArgumentException}
     *
     * @param monetaryAmount
     */
    public Money plus(final Money monetaryAmount) {
        checkCurrencies(monetaryAmount);

        final BigDecimal newAmount = getAmount().add(monetaryAmount.getAmount());
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /**
     * If currencies match subtracts passed amount from this amount and returns result as new instance. Otherwise throws {@link IllegalArgumentException}
     *
     * @param monetaryAmount
     */
    public Money minus(final Money monetaryAmount) {
        checkCurrencies(monetaryAmount);

        final BigDecimal newAmount = getAmount().subtract(monetaryAmount.getAmount());
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /**
     * Multiplies current amount by passed value, rounds the result using {@link RoundingMode#HALF_EVEN} rule with 2 digits after comma and returns result as new instance.
     *
     * @param value
     */
    public Money multiply(final BigDecimal value) {
        final BigDecimal newAmount = getAmount().multiply(value);
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /**
     * This is a convenience method based on {@link #multiply(BigDecimal)}.
     *
     * @param value
     */
    public Money multiply(final int value) {
        return multiply(new BigDecimal(value));
    }

    /**
     * Divides current amount by passed value with rounding rule {@link RoundingMode#HALF_EVEN} and returns result as new instance.
     *
     * @param value
     */
    public Money divide(final BigDecimal value) {
        final BigDecimal newAmount = getAmount().divide(value, HALF_EVEN);
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /**
     * This is a convenience method based on {@link #divide(BigDecimal)}.
     *
     * @param value
     */
    public Money divide(final int value) {
        return divide(new BigDecimal(value));
    }

    /**
     * Splits current amount into certain parts and returns them as list. Throws an exception if passed parameter is less than 1.
     *
     * @param value
     */
    public List<Money> split(final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Could only proportionally divide by values greater than zero");
        }
        if (value == 1) {
            // there is nothing to divide by... so returning this instance -- it is safe due to immutability
            return Arrays.asList(this); // returns immutable List instance
        } else {
            // dividing into more than one part
            final List<Money> parts = new ArrayList<>();
            // defining index of happy man who may receive larger part
            final int luckyIndex = random.nextInt(value);
            // calculating amount of money for each
            final BigDecimal newAmount = getAmount().divide(new BigDecimal(value), HALF_EVEN);
            final Money newMonetaryAmount = (getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency()));
            for (int index = 0; index < value; index++) {
                if (index != luckyIndex) {
                    parts.add(newMonetaryAmount);
                } else {
                    final BigDecimal luckyAmount = getAmount().subtract(new BigDecimal(value - 1).multiply(newAmount));
                    final Money luckyMonetaryAmount = (getTaxPercent() != null ? new Money(luckyAmount, getTaxPercent(), getCurrency()) : new Money(luckyAmount, getCurrency()));
                    parts.add(luckyMonetaryAmount);
                }
            }
            return unmodifiableList(parts);
        }
    }

    /**
     * Returns true if and only if currency and amount matches.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }

        final Money monetaryAmount = (Money) o;

        if (!currency.equals(monetaryAmount.currency)) {
            return false;
        }
        return amount.compareTo(monetaryAmount.amount) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        result = amount.hashCode();
        result = 29 * result + currency.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
        currencyInstance.setCurrency(currency);
        currencyInstance.setMinimumFractionDigits(2);
        currencyInstance.setMaximumFractionDigits(4);
        return currencyInstance.format(getAmount());
    }

    /**
     * If currencies match compares their amounts, otherwise throws {@link IllegalArgumentException}
     */
    @Override
    public int compareTo(final Money monetaryAmount) {
        checkCurrencies(monetaryAmount);

        return getAmount().compareTo(monetaryAmount.getAmount());
    }

    /**
     * Throws {@link IllegalArgumentException} if currencies doesn't match
     *
     * @param monetaryAmount
     */
    private void checkCurrencies(final Money monetaryAmount) {
        // FIXME at this stage the platform is not ready to be so serious about currency missmatch
        //	if (!getCurrency().equals(monetaryAmount.getCurrency())) {
        //	    throw new IllegalArgumentException("Can only operate on monetary amounts of the same currency");
        //	}
    }

    /**
     * Throws {@link IllegalArgumentException} if any of parameters is null
     *
     * @param value
     * @param currency
     */
    private BigDecimal checkParameters(final BigDecimal amount, final Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency should not be null");
        }

        if (amount != null) {
            return amount.setScale(4, HALF_EVEN);
        } else {
            return BigDecimal.ZERO.setScale(4, HALF_EVEN);
        }
    }

    /**
     * In addition to {@link #checkParameters(BigDecimal, Currency)} validates the <code>taxPersent</code>, which should be between 1 and 100 inclusive.
     *
     * @param amount
     * @param taxPercent
     * @param currency
     */
    private BigDecimal checkParameters(final BigDecimal amount, final int taxPercent, final Currency currency) {
        if (taxPercent < 1 || taxPercent > 100) {
            throw new IllegalArgumentException(format("Tax percentage [%s] should not be outside of period [1,100].", taxPercent));
        }
        return checkParameters(amount, currency);
    }

    private BigDecimal checkParameters(final BigDecimal amount, final BigDecimal taxAmount, final Currency currency) {
        if (taxAmount == null) {
            throw new IllegalArgumentException("Tax amount should not be null");
        }
        if (taxAmount.doubleValue() < 0) {
            throw new IllegalArgumentException("Tax amount should not be negative");
        }

        final BigDecimal updatedAmount = checkParameters(amount, currency);
        if (updatedAmount.compareTo(taxAmount) < 0) {
            throw new IllegalArgumentException("Amount should not be less than its tax amount");
        }

        return updatedAmount;
    }

    public BigDecimal getExTaxAmount() {
        return exTaxAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public Integer getTaxPercent() {
        return taxPercent;
    }

    /**
     * Less than.
     */
    public boolean lt(final Money amount) {
        return compareTo(amount) < 0;
    }

    /**
     * Less than or equal.
     */
    public boolean le(final Money amount) {
        return compareTo(amount) <= 0;
    }

    /**
     * Equal.
     */
    public boolean eq(final Money amount) {
        return compareTo(amount) == 0;
    }

    /**
     * Greater than.
     */
    public boolean gt(final Money amount) {
        return compareTo(amount) > 0;
    }

    /**
     * Greater than or equal.
     */
    public boolean ge(final Money amount) {
        return compareTo(amount) >= 0;
    }

}