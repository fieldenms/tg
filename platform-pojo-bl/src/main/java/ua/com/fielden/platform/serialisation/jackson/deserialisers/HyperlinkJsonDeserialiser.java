package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ua.com.fielden.platform.types.Hyperlink;

public class HyperlinkJsonDeserialiser extends StdDeserializer<Hyperlink> {

    private static final long serialVersionUID = 1L;

    public HyperlinkJsonDeserialiser() {
		super(Hyperlink.class);
	}

    @Override
    public Hyperlink deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();
        final String linkValue = node.get("value").isNull() ? null : node.get("value").textValue();
        return linkValue == null ? null : new Hyperlink(linkValue);
    }
}
