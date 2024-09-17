package ua.com.fielden.platform.web.resources.webui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Jackson JSON serialiser for {@link CriteriaIndication}.
 */
public class CriteriaIndicationJsonSerialiser extends StdSerializer<CriteriaIndication> {

    public CriteriaIndicationJsonSerialiser() {
        super(CriteriaIndication.class);
    }

    @Override
    public void serialize(final CriteriaIndication value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("name");
        generator.writeObject(value.name());
        generator.writeFieldName("message");
        generator.writeObject(value.message);
        generator.writeFieldName("style");
        generator.writeObject(value.style);
        generator.writeEndObject();
    }

}