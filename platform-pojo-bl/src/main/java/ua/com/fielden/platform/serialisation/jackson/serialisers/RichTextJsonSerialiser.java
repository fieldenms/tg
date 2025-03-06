package ua.com.fielden.platform.serialisation.jackson.serialisers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextJsonDeserialiser;

import java.io.IOException;

import static ua.com.fielden.platform.types.RichText.*;

/**
 * JSON serialiser for {@link RichText}.
 * <p>
 * {@link RichTextJsonDeserialiser} specifies possible forms of a serialised {@link RichText} object.
 */
public class RichTextJsonSerialiser extends StdSerializer<RichText> {

    public RichTextJsonSerialiser() {
        super(RichText.class);
    }

    @Override
    public void serialize(final RichText richText, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException
    {
        generator.writeStartObject();

        final var validationResult = richText.isValid();
        if (validationResult.isSuccessful()) {
            generator.writeFieldName(FORMATTED_TEXT);
            generator.writeObject(richText.formattedText());
            generator.writeFieldName(CORE_TEXT);
            generator.writeObject(richText.coreText());
        }
        else {
            generator.writeFieldName(FORMATTED_TEXT);
            generator.writeObject(null);
            generator.writeFieldName(CORE_TEXT);
            generator.writeObject(null);
            generator.writeFieldName(VALIDATION_RESULT);
            generator.writeObject(validationResult);
        }

        generator.writeEndObject();
    }

}
