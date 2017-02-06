package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.types.Hyperlink;

public class HyperlinkJsonSerialiser extends StdSerializer<Hyperlink> {

    public HyperlinkJsonSerialiser() {
        super(Hyperlink.class);
    }

    @Override
    public void serialize(final Hyperlink link, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeStartObject();
        generator.writeFieldName("value");
        generator.writeObject(link.toString());
        generator.writeEndObject();
    }

}