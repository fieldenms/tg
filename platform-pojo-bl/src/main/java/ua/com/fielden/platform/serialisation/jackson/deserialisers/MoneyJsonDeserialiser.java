package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import ua.com.fielden.platform.types.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class MoneyJsonDeserialiser extends StdDeserializer<Money> {

    public MoneyJsonDeserialiser() {
        super(Money.class);
    }

    @Override
    public Money deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();

        final BigDecimal amount = node.get("amount").decimalValue();
        final String currencyStr = node.get("currency").textValue();
        final Integer taxPercentage = node.get("taxPercent").intValue();

        return taxPercentage == null ? new Money(amount, Currency.getInstance(currencyStr)) : new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
    }
}
