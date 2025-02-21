package ua.com.fielden.platform.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ua.com.fielden.platform.serialisation.jackson.exceptions.DeserialisationException;

import java.io.IOException;

public class RichTextJsonDeserialiser extends StdDeserializer<RichText> {

    public static final String ERR_UNEXPECTED_NULL_IN_FIELD_FORMATTED_TEXT = "Unexpected null in field [formattedText] in object %s.";
    public static final String ERR_UNEXPECTED_NULL_IN_FIELD_CORE_TEXT = "Unexpected null in field [coreText] in object %s.";
    public static final String ERR_MISSING_FIELD = "Missing field [%s] in object %s.";
    public static final String ERR_WAS_EXPECTING_STRING = "Expected string in field [%s] but was %s, in object %s.";

    public RichTextJsonDeserialiser() {
        super(RichText.class);
    }

    @Override
    public RichText deserialize(final JsonParser parser, final DeserializationContext ctx) throws IOException {
        final JsonNode node = parser.readValueAsTree();
        final var formattedTextNode = requireField(node, RichText.FORMATTED_TEXT);
        final var coreTextNode = requireField(node, RichText.CORE_TEXT);

        // if all components are null, treat the whole as null
        if (formattedTextNode.isNull() && coreTextNode.isNull()) {
            return null;
        }
        if (formattedTextNode.isNull()) {
            throw new DeserialisationException(ERR_UNEXPECTED_NULL_IN_FIELD_FORMATTED_TEXT.formatted(node.toPrettyString()));
        }
        if (coreTextNode.isNull()) {
            throw new DeserialisationException(ERR_UNEXPECTED_NULL_IN_FIELD_CORE_TEXT.formatted(node.toPrettyString()));
        }

        final String formattedText = requireText(formattedTextNode,  RichText.FORMATTED_TEXT, node);
        final String coreText = requireText(coreTextNode, RichText.CORE_TEXT, node);
        return new RichText.Persisted(formattedText, coreText);
    }

    private static JsonNode requireField(final JsonNode node, final String name) {
        final var subNode = node.get(name);
        if (subNode == null) {
            throw new DeserialisationException(ERR_MISSING_FIELD.formatted(name, node.toPrettyString()));
        }
        return subNode;
    }

    /**
     * @param fieldName  name of the field that links node to parent node
     */
    private static String requireText(final JsonNode node, final String fieldName, final JsonNode parentNode) {
        final String text = node.textValue();
        if (text == null) {
            throw new DeserialisationException(ERR_WAS_EXPECTING_STRING.formatted(
                    fieldName, node.getNodeType(), parentNode.toPrettyString()));
        }
        return text;
    }

}
