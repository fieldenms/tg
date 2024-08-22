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
        generator.writeFieldName(RichText._formattedText);
        generator.writeObject(richText.formattedText());
        generator.writeFieldName(RichText._coreText);
        generator.writeObject(richText.coreText());
        generator.writeEndObject();
    }

}
