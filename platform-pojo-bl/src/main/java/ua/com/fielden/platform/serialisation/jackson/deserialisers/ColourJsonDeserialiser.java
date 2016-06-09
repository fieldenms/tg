package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ua.com.fielden.platform.types.Colour;

public class ColourJsonDeserialiser extends StdDeserializer<Colour> {

    public ColourJsonDeserialiser() {
		super(Colour.class);
	}

    @Override
    public Colour deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();
        final String hashlessUppercasedColourValue = node.get("hashlessUppercasedColourValue").isNull() ? null : node.get("hashlessUppercasedColourValue").textValue();
        return hashlessUppercasedColourValue == null ? null : new Colour(hashlessUppercasedColourValue);
    }
}
