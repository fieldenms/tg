package ua.com.fielden.platform.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.jackson.exceptions.DeserialisationException;

import java.io.IOException;

import static ua.com.fielden.platform.types.RichText.*;

/**
 * JSON deserialiser for {@link RichText}.
 * <p>
 * The result of deserialisation is one of the following:
 * <ol>
 *   <li> {@code null} - if formatted and core text are {@code null}, and validation result is absent.
 *   <li> {@link RichText.Invalid} - if validation result is present.
 *        A validation result is expected to be unsuccessful, it is an error otherwise.
 *   <li> Valid {@link RichText} - if both formatted and core text are present and are not {@code null}.
 * </ol>
 * If neither of the above matches, it is an error.
 */
public class RichTextJsonDeserialiser extends StdDeserializer<RichText> {

    public static final String ERR_UNEXPECTED_NULL_IN_FIELD = "Unexpected null in field [%s] in object %s.";
    public static final String ERR_MISSING_FIELD = "Missing field [%s] in object %s.";
    public static final String ERR_WAS_EXPECTING_STRING = "Expected string in field [%s] but was %s, in object %s.";
    private final ObjectMapper mapper;

    public RichTextJsonDeserialiser(final ObjectMapper mapper) {
        super(RichText.class);
        this.mapper = mapper;
    }

    @Override
    public RichText deserialize(final JsonParser parser, final DeserializationContext ctx) throws IOException {
        final JsonNode node = parser.readValueAsTree();
        final var formattedTextNode = requireField(node, FORMATTED_TEXT);
        final var coreTextNode = requireField(node, CORE_TEXT);

        if (formattedTextNode.isNull() && coreTextNode.isNull()) {
            final var validationResultNode = node.get(VALIDATION_RESULT);
            if (validationResultNode != null) {
                if (validationResultNode.isNull()) {
                    throw new DeserialisationException(ERR_UNEXPECTED_NULL_IN_FIELD.formatted(VALIDATION_RESULT, node.toPrettyString()));
                }
                final var result = validationResultNode.traverse(mapper).readValueAs(Result.class);
                return fromUnsuccessfulValidationResult(result);
            }
            // if all components are null, treat the whole as null
            return null;
        }
        if (formattedTextNode.isNull()) {
            throw new DeserialisationException(ERR_UNEXPECTED_NULL_IN_FIELD.formatted(FORMATTED_TEXT, node.toPrettyString()));
        }
        if (coreTextNode.isNull()) {
            throw new DeserialisationException(ERR_UNEXPECTED_NULL_IN_FIELD.formatted(CORE_TEXT, node.toPrettyString()));
        }

        final String formattedText = requireText(formattedTextNode,  FORMATTED_TEXT, node);
        final String coreText = requireText(coreTextNode, CORE_TEXT, node);
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
