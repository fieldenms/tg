package ua.com.fielden.platform.serialisation.jackson.serialisers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.com.fielden.platform.types.RichText;

import java.io.IOException;

public class RichTextJsonSerialiser extends StdSerializer<RichText> {

    public RichTextJsonSerialiser() {
        super(RichText.class);
    }

    @Override
    public void serialize(final RichText richText, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException
    {
        generator.writeStartObject();

        if (richText.isValid().isSuccessful()) {
            generator.writeFieldName(RichText.FORMATTED_TEXT);
            generator.writeObject(richText.formattedText());
            generator.writeFieldName(RichText.CORE_TEXT);
            generator.writeObject(richText.coreText());
        }
        else {
            generator.writeFieldName(RichText.FORMATTED_TEXT);
            generator.writeObject(null);
            generator.writeFieldName(RichText.CORE_TEXT);
            generator.writeObject(null);
        }

        generator.writeEndObject();
    }

}
