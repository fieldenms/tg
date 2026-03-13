package ua.com.fielden.platform.serialisation.jackson.serialisers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.com.fielden.platform.types.Money;

import java.io.IOException;

import static ua.com.fielden.platform.types.Money.*;

/**
 * Serialiser for {@link Money} type.
 *
 * @author TG Team
 *
 */
public class MoneyJsonSerialiser extends StdSerializer<Money> {

    public MoneyJsonSerialiser() {
        super(Money.class);
    }

    @Override
    public void serialize(final Money money, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();

        generator.writeFieldName(AMOUNT);
        generator.writeObject(money.getAmount());
        generator.writeFieldName(CURRENCY);
        generator.writeObject(money.getCurrency().toString());
        generator.writeFieldName(TAX_PERCENT);
        generator.writeObject(money.getTaxPercent());

        generator.writeEndObject();
    }

}
