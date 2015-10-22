package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.types.Colour;

public class ColourJsonSerialiser extends StdSerializer<Colour> {

    public ColourJsonSerialiser() {
        super(Colour.class);
    }

    @Override
    public void serialize(final Colour colourValue, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeStartObject();
        generator.writeFieldName("hashlessUppercasedColourValue");
        generator.writeObject(colourValue.toString());
        generator.writeEndObject();
    }

}