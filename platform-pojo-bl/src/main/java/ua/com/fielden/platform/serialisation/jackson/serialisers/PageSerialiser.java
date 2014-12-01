package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;

import ua.com.fielden.platform.pagination.IPage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@SuppressWarnings("rawtypes")
public class PageSerialiser extends JsonSerializer<IPage> {

    @Override
    public void serialize(final IPage page, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();
        generator.writeFieldName("pageNo");
        generator.writeObject(page.no());
        generator.writeFieldName("pageCount");
        generator.writeObject(page.numberOfPages());
        generator.writeFieldName("data");
        generator.writeObject(page.data());
        generator.writeFieldName("summary");
        generator.writeObject(page.summary());
        generator.writeEndObject();
    }

}
