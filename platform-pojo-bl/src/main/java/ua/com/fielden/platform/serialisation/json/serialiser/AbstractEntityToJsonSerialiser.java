package ua.com.fielden.platform.serialisation.json.serialiser;

import java.io.IOException;
import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@SuppressWarnings("rawtypes")
public class AbstractEntityToJsonSerialiser extends JsonSerializer<AbstractEntity> {

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final AbstractEntity entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();
        for (final Field propertyField : Finder.findRealProperties(entity.getType())) {
            generator.writeFieldName(propertyField.getName());

            propertyField.setAccessible(true);
            Object value = null;
            try {
                value = propertyField.get(entity);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            propertyField.setAccessible(false);

            generator.writeObject(value);

        }
        generator.writeEndObject();
    }

}
