package ua.com.fielden.platform.types;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.markers.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Currency.getInstance;
import static java.util.Locale.getDefault;

/// An immutable representation of money in a particular currency.
///
/// All monetary arithmetic operations are performed with 4 decimal places using [RoundingMode#HALF_EVEN] rounding rule.
/// Method [#toString] rounds up the amount to 2 decimal places for display purposes only.
///
/// Although this type declares several properties, the set of actual properties that will be available depends on the configured
/// Hibernate type ([ISimpleMoneyType], [IMoneyType], [ISimplyMoneyWithTaxAmountType], [ISimplyMoneyWithTaxAndExTaxAmountType], [IMoneyWithTaxAmountType]).
///
/// ### Tax support
/// Property `amount` can represent either a tax inclusive or exclusive amount if used in non-tax sensitive situations.
/// However, when used in a tax-sensitive context, it should at all times represent a full amount (i.e. tax inclusive).
/// Properties `exTaxAmount` and `taxAmount` correspond to amount without tax (e.g. without GST) and the tax amount (e.g. GST value) respectively.
/// If money is used in a non-tax sensitive context, then `exTaxAmount` and `taxAmount` are null.
/// However, if used in a tax-sensitive context, then it is assumed that `amount = (taxPercent/100)*exTaxAmount + exTaxAmount`.
///
/// There are several constructors that can be conveniently used for creation of tax and non-tax sensitive instances.
///
/// This representation assumes that `taxPercent` can always be expressed as an integer value.
///
/// ### Important details
/// 1. All monetary arithmetic operations produce tax sensitive instances if the instance operated on is tax sensitive.
/// 2. Methods [#equals] and [#hashCode] use only properties `amount` and `currency`, while [#compareTo(Money)] uses only `amount`.
///
/// ### Use in calculated properties
/// [Money] can be used as a type of a calculated property.
///
/// * The main expression will be associated with property [Money#amount].
///
/// * If [Money#currency] is enabled, an extra expression can be specified for it.
///
///   This expression can also be inferred.
///   The inference rule is this: the first `Money`-typed property that occurs in _tail position_.
///
///   To be in tail position, an operand must be _a part of the resulting value_ (optionally subject to conditional expressions).
///
///   ```
///   // In tail position: prop("price")
///   expr().prop("price").model()
///
///   // In tail position: prop("price"), val(5)
///   expr().prop("price").mult().val(5).model()
///
///   // In tail position: prop("price")
///   expr().sumOf().prop("price").model()
///
///   // In tail position: prop("price"), prop("prevPrice"), val(2), prop("purchasePrice"), val(1)
///   expr()
///   .caseWhen().prop("key")...
///     .then().prop("price)
///   .when()...
///     .then().prop("prevPrice").div().val(2)
///   .otherwise()
///     .prop("purchasePrice").add().val(1)
///   .model()
///
///   // In tail position: prop("prevPrice"), prop("price")
///   expr().ifNull().prop("prevPrice").then().prop("price").model()
///   ```
///
///   Inference does not apply in the following cases:
///
///   * The `Money` expression does not contain a matching property in tail position.
///
///     ```java
///     expr().val(50).model()
///     ```
///
///   * A sub-query in tail position.
///
///     ```java
///     expr()
///     .select(...) ... modelAsPrimitive()
///     .model()
///     ```
/// ---
/// For more details on calculated properties refer to [Calculated].
///
public class Money implements Comparable<Money> {

    public static final String
            AMOUNT = "amount",
            EX_TAX_AMOUNT = "exTaxAmount",
            TAX_AMOUNT = "taxAmount",
            TAX_PERCENT = "taxPercent",
            CURRENCY = "currency";

    private static final Random random = new Random();

    /// This is a convenient unit element for addition.
    public static final Money zero = new Money("0.00");

    /// This is a convenient unit element for multiplication.
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

    /// The currency of the monetary amount.
    ///
    /// If enabled, must not be null.
    /// This requirement also applies to calculated properties and properties populated by synthetic models.
    /// In those cases, the calculated/yielded currency must not be null.
    ///
    @IsProperty
    @MapTo
    private final Currency currency;

    /// Creates an instance without tax data.
    /// Null values are assigned to tax-related properties.
    ///
    /// @param amount  monetary amount, which could be either tax inclusive or exclusive, depending on the usage context.
    ///
    public Money(final BigDecimal amount, final Currency currency) {
        this.amount = checkParameters(amount, currency);
        exTaxAmount = null;
        taxAmount = null;
        taxPercent = null;
        this.currency = currency;
    }

    /// Creates an instance without tax data.
    /// Null values are assigned to tax-related properties.
    ///
    /// @param amount  monetary amount, which could be either tax inclusive or exclusive, depending on the usage context.
    ///
    public Money(final String amount, final Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    /// Creates an instance with tax data.
    ///
    /// @param amount      monetary amount tax inclusive.
    /// @param taxPercent  tax percent used for calculation of the tax amount and ex-tax amount.
    ///
    public Money(final BigDecimal amount, final int taxPercent, final Currency currency) {
        this.amount = checkParameters(amount, taxPercent, currency);
        this.taxPercent = taxPercent;
        final BigDecimal taxFrac = new BigDecimal(taxPercent / 100d, new MathContext(4));
        exTaxAmount = new BigDecimal(1d, new MathContext(50)).divide(taxFrac.add(BigDecimal.ONE), new MathContext(50, HALF_EVEN)).multiply(this.amount).setScale(4, HALF_EVEN);
        taxAmount = amount.subtract(exTaxAmount).setScale(4, HALF_EVEN);
        this.currency = currency;
    }

    /// Creates an instance with tax data.
    ///
    /// @param amount      monetary amount tax inclusive.
    /// @param taxPercent  tax percent used for calculation of the tax amount and ex-tax amount.
    ///
    public Money(final String amount, final int taxPercent, final Currency currency) {
        this(new BigDecimal(amount), taxPercent, currency);
    }

    /// Creates an instance with tax data.
    ///
    /// @param amount     monetary amount tax inclusive.
    /// @param taxAmount  tax amount, which is a tax portion of the provided amount; used for calculation of tax percent and ex-tax amount.
    ///
    public Money(final BigDecimal amount, final BigDecimal taxAmount, final Currency currency) {
        this.amount = checkParameters(amount, taxAmount, currency);
        this.taxAmount = taxAmount.setScale(4, HALF_EVEN);
        this.taxPercent = taxAmount.multiply(BigDecimal.valueOf(100.0000d)).divide(this.amount.subtract(taxAmount), HALF_UP).setScale(0, HALF_EVEN).intValue();
        exTaxAmount = getAmount().subtract(taxAmount).setScale(4, HALF_EVEN);
        this.currency = currency;
    }

    /// Creates an instance using the default currency.
    /// Null values are assigned to tax-related properties.
    ///
    /// @param amount  monetary amount, which could be either tax inclusive or exclusive, depending on the usage context.
    ///
    public Money(final BigDecimal amount) {
        this(amount, getInstance(getDefault()));
    }

    /// Creates an instance using the default currency.
    /// Null values are assigned to tax-related properties.
    ///
    /// @param amount  monetary amount, which could be either tax inclusive or exclusive, depending on the usage context.
    ///
    public Money(final String amount) {
        this(new BigDecimal(amount));
    }

    /// Creates an instance using the default currency.
    /// Null values are assigned to tax-related properties.
    ///
    /// @param amount  monetary amount, which could be either tax inclusive or exclusive, depending on the usage context.
    ///
    public static Money of(final String amount) {
        return new Money(amount);
    }

    public Currency getCurrency() {
        return currency;
    }

    /// Returns a [Money] instance equal to this one but with the specified currency.
    ///
    public Money withCurrency(final Currency currency) {
        return getCurrency().equals(currency)
                ? this
                : getTaxPercent() != null ? new Money(getAmount(), getTaxPercent(), currency) : new Money(getAmount(), currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /// Adds two amounts, reusing the tax percent and currency of this instance.
    ///
    public Money plus(final Money money) {
        final BigDecimal newAmount = getAmount().add(money.getAmount());
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /// Subtracts `money` from this amount, reusing the tax percent and currency of this instance.
    ///
    public Money minus(final Money money) {
        final BigDecimal newAmount = getAmount().subtract(money.getAmount());
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /// Multiplies this amount by `value`, reusing the tax percent and currency of this instance.
    /// Rounds the result using [RoundingMode#HALF_EVEN] rule with 2 digits after comma.
    ///
    public Money multiply(final BigDecimal value) {
        final BigDecimal newAmount = getAmount().multiply(value);
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /// Multiplies this amount by `value`, reusing the tax percent and currency of this instance.
    /// Rounds the result using [RoundingMode#HALF_EVEN] rule with 2 digits after comma.
    ///
    public Money multiply(final int value) {
        return multiply(new BigDecimal(value));
    }

    /// Divides this amount by `value`, reusing the tax percent and currency of this instance.
    /// Rounds the result using [RoundingMode#HALF_EVEN] rule with 2 digits after comma.
    ///
    public Money divide(final BigDecimal value) {
        final BigDecimal newAmount = getAmount().divide(value, HALF_EVEN);
        return getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency());
    }

    /// Divides this amount by `value`, reusing the tax percent and currency of this instance.
    /// Rounds the result using [RoundingMode#HALF_EVEN] rule with 2 digits after comma.
    ///
    public Money divide(final int value) {
        return divide(new BigDecimal(value));
    }

    /// Splits this amount into a number of even parts, all of which inherit the tax percent and currency of this instance.
    ///
    /// @param value  the number of parts to split into; must be greater or equal to 1.
    ///
    public List<Money> split(final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Could only proportionally divide by values greater than zero");
        }
        if (value == 1) {
            return ImmutableList.of(this);
        } else {
            final var newAmount = getAmount().divide(new BigDecimal(value), HALF_EVEN);
            final var evenMoneyPart = (getTaxPercent() != null ? new Money(newAmount, getTaxPercent(), getCurrency()) : new Money(newAmount, getCurrency()));

            // Index of the happy man who may receive the larger part.
            final int luckyIndex = random.nextInt(value);
            final var luckyAmount = getAmount().subtract(new BigDecimal(value - 1).multiply(newAmount));
            final var luckyMoneyPart = getTaxPercent() != null ? new Money(luckyAmount, getTaxPercent(), getCurrency()) : new Money(luckyAmount, getCurrency());

            return IntStream.range(0, value)
                    .mapToObj(i -> i == luckyIndex ? luckyMoneyPart : evenMoneyPart)
                    .collect(toImmutableList());
        }
    }

    /// Returns true if and only if currency and amount match.
    ///
    @Override
    public boolean equals(final Object o) {
        return this == o
                || o instanceof Money that
                && currency.equals(that.currency)
                && amount.compareTo(that.amount) == 0;
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

    /// Compares the amounts.
    ///
    @Override
    public int compareTo(final Money monetaryAmount) {
        return getAmount().compareTo(monetaryAmount.getAmount());
    }

    // TODO At this stage the platform is not ready to be so serious about currency missmatch.
    // private void checkCurrencies(final Money monetaryAmount) {
    //     if (!getCurrency().equals(monetaryAmount.getCurrency())) {
    //         throw new IllegalArgumentException("Can only operate on monetary amounts of the same currency");
    //     }
    // }

    private BigDecimal checkParameters(final BigDecimal amount, final Currency currency) {
        if (currency == null) {
            throw new InvalidArgumentException("Currency must not be null");
        }

        if (amount != null) {
            return amount.setScale(4, HALF_EVEN);
        } else {
            return BigDecimal.ZERO.setScale(4, HALF_EVEN);
        }
    }

    private BigDecimal checkParameters(final BigDecimal amount, final int taxPercent, final Currency currency) {
        if (taxPercent < 1 || taxPercent > 100) {
            throw new InvalidArgumentException(format("Tax percentage [%s] must be within [1, 100].", taxPercent));
        }
        return checkParameters(amount, currency);
    }

    private BigDecimal checkParameters(final BigDecimal amount, final BigDecimal taxAmount, final Currency currency) {
        if (taxAmount == null) {
            throw new IllegalArgumentException("Tax amount must not be null");
        }
        if (taxAmount.doubleValue() < 0) {
            throw new IllegalArgumentException("Tax amount must not be negative");
        }

        final BigDecimal updatedAmount = checkParameters(amount, currency);
        if (updatedAmount.compareTo(taxAmount) < 0) {
            throw new IllegalArgumentException("Amount must not be less than its tax amount");
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

    /// Less than.
    ///
    public boolean lt(final Money amount) {
        return compareTo(amount) < 0;
    }

    /// Less than or equal.
    ///
    public boolean le(final Money amount) {
        return compareTo(amount) <= 0;
    }

    /// Equal.
    ///
    public boolean eq(final Money amount) {
        return compareTo(amount) == 0;
    }

    /// Greater than.
    ///
    public boolean gt(final Money amount) {
        return compareTo(amount) > 0;
    }

    /// Greater than or equal.
    ///
    public boolean ge(final Money amount) {
        return compareTo(amount) >= 0;
    }

}
