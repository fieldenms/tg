package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ua.com.fielden.platform.types.Money;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import static ua.com.fielden.platform.types.Money.*;

public class MoneyJsonDeserialiser extends StdDeserializer<Money> {

    public MoneyJsonDeserialiser() {
        super(Money.class);
    }

    @Override
    public Money deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();

        final BigDecimal amount = node.get(AMOUNT).isNull() ? null : node.get(AMOUNT).decimalValue();
        final String currencyStr = node.get(CURRENCY).isNull() ? null : node.get(CURRENCY).textValue();
        final Integer taxPercentage = node.get(TAX_PERCENT).isNull() ? null : node.get(TAX_PERCENT).intValue();

        return taxPercentage == null ? new Money(amount, Currency.getInstance(currencyStr)) : new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
    }
}
