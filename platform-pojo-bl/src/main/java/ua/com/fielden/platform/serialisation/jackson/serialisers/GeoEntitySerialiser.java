package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;
import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeoEntitySerialiser extends JsonSerializer<AbstractEntity> {

    @Override
    public void serialize(final AbstractEntity entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();

        //        generator.writeFieldName("type");
        //        generator.writeObject("Feature");
        //        generator.writeFieldName("id");
        //        generator.writeObject(null);
        //        generator.writeFieldName("geometry");
        //        generator.writeStartObject();
        //        generator.writeEndObject();

        generator.writeFieldName("properties"); // this is necessary to make it geoJson compatible (not to rebuild the tree again)
        generator.writeStartObject();
        generator.writeFieldName("_entityType");
        generator.writeObject(DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.stripIfNeeded(entity.getType())).getSimpleName());

        for (final Field propertyField : Finder.findRealProperties((Class<? extends AbstractEntity<?>>) entity.getType())) {
            if (!AbstractEntity.KEY.equals(propertyField.getName()) // disregard inclusion of composite key property inside json (as the separate parts of keys will be included)
                    || !DynamicEntityKey.class.equals(PropertyTypeDeterminator.determinePropertyType(entity.getType(), AbstractEntity.KEY))) {

                final boolean wasAccessible = propertyField.isAccessible();
                propertyField.setAccessible(true);
                Object value = null;
                try {
                    value = propertyField.get(entity);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                propertyField.setAccessible(wasAccessible);

                if (value != null) { // disregard inclusion of property with null value
                    generator.writeFieldName(propertyField.getName());
                    generator.writeObject(value);
                }
            }
        }
        generator.writeEndObject();

        generator.writeEndObject();
    }

}
