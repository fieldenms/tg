package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serialiser for {@link Result} type.
 *
 * @author TG Team
 *
 */
public class ResultJsonSerialiser extends StdSerializer<Result> {

    public ResultJsonSerialiser() {
        super(Result.class);
    }

    @Override
    public void serialize(final Result result, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();

        generator.writeFieldName("@resultType");
        generator.writeObject(result.getClass().getName());
        generator.writeFieldName("message");
        generator.writeObject(result.getMessage());

        if (result.getInstance() != null) {
            generator.writeFieldName("@instanceType");
            generator.writeObject(PropertyTypeDeterminator.stripIfNeeded(result.getInstance().getClass()).getName());
            generator.writeFieldName("instance");
            generator.writeObject(result.getInstance());
        }

        if (result.getEx() != null) {
            generator.writeFieldName("ex");
            generator.writeObject(result.getEx());
        }

        generator.writeEndObject();
    }

}
