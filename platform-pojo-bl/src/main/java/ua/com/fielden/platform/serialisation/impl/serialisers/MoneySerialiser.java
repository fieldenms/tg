package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Currency;

import ua.com.fielden.platform.types.Money;

import com.esotericsoftware.kryo.Kryo;

/**
 * Serialises {@link Money} instances. Only properties amount, currency and taxPercentage are take into account during serialisation.
 * 
 * @author TG Team
 * 
 */
public class MoneySerialiser extends TgSimpleSerializer<Money> {

    public MoneySerialiser(final Kryo kryo) {
        super(kryo);
    }

    @Override
    public void write(final ByteBuffer buffer, final Money money) {
        writeValue(buffer, money.getAmount());
        writeValue(buffer, money.getCurrency().toString());
        writeValue(buffer, money.getTaxPercent());
    }

    @Override
    public Money read(final ByteBuffer buffer) {
        final BigDecimal amount = readValue(buffer, BigDecimal.class);
        final String currencyStr = readValue(buffer, String.class);
        final Integer taxPercentage = readValue(buffer, Integer.class);

        return taxPercentage == null ? new Money(amount, Currency.getInstance(currencyStr)) : new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
    }

}
