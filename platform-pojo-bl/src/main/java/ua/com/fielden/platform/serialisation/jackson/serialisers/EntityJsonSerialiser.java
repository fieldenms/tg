package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EntityJsonSerialiser<T extends AbstractEntity<?>> extends JsonSerializer<T> {
    private final Class<T> type;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<CachedProperty> properties;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties) {
        this.type = type;
        this.properties = properties;
    }

    @Override
    public void serialize(final T entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();

        // serialise entity type
        generator.writeFieldName("@entityType");
        generator.writeObject(entity.getType().getName());

        // serialise id
        generator.writeFieldName(AbstractEntity.ID);
        generator.writeObject(entity.getId());

        // serialise version -- should never be null
        generator.writeFieldName(AbstractEntity.VERSION);
        generator.writeObject(entity.getVersion());

        // serialise all the properties relying on the fact that property sequence is consistent with order of fields in the class declaration
        for (final CachedProperty prop : properties) {
            // non-composite keys should be persisted by identifying their actual type
            final String name = prop.field().getName();
            Object value = null;
            try {
                // at this stage the field should be already accessible
                value = prop.field().get(entity);
            } catch (final IllegalAccessException e) {
                // developer error -- please ensure that all fields are accessible
                e.printStackTrace();
                logger.error("The field [" + prop.field() + "] is not accessible. Fatal error during serialisation process for entity [" + entity + "].", e);
                throw new RuntimeException(e);
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
                logger.error("The field [" + prop.field() + "] is not declared in entity with type [" + type.getName() + "]. Fatal error during serialisation process for entity [" + entity + "].", e);
                throw e;
            }

            final boolean dirty = entity.getProperty(name) == null ? false : entity.getProperty(name).isDirty();

            // write actual property
            generator.writeFieldName(name);
            if (dirty && value != null) {
                generator.writeObject(value);
            } else if (dirty && value == null) {
                generator.writeObject(null);
            } else {
                // TODO Theoretically the following two conditions can be removed when serialising from the client side due to the fact that server can retrieve the data from db if required
                if (!dirty && value != null) {
                    generator.writeObject(value);
                } else if (!dirty && value == null) {
                    generator.writeObject(null);
                }
            }

            // write actual property
            generator.writeFieldName("@" + name);
            generator.writeStartObject();

            generator.writeFieldName("dirty");
            generator.writeObject(dirty);

            generator.writeEndObject();
        }

        generator.writeEndObject();
    }

}
