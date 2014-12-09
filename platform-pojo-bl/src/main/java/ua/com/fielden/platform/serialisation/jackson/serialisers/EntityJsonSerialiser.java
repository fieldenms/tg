package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfo;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EntityJsonSerialiser<T extends AbstractEntity<?>> extends StdSerializer<T> {
    private final Class<T> type;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<CachedProperty> properties;
    private final EntityTypeInfo entityTypeInfo;
    private final DefaultValueContract defaultValueContract;
    private final boolean excludeNulls;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties, final EntityTypeInfo entityTypeInfo, final DefaultValueContract defaultValueContract, final boolean excludeNulls) {
        super(type);
        this.type = type;
        this.properties = properties;
        this.entityTypeInfo = entityTypeInfo;
        this.defaultValueContract = defaultValueContract;
        this.excludeNulls = excludeNulls;
    }

    @Override
    public void serialize(final T entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        ////////////////////////////////////////////////////
        ///////////////// handle references ////////////////
        ////////////////////////////////////////////////////
        final JacksonContext context = EntitySerialiser.getContext();
        References references = (References) context.getTemp(EntitySerialiser.ENTITY_JACKSON_REFERENCES);
        if (references == null) {
            // Use non-temporary storage to avoid repeated allocation.
            references = (References) context.get(EntitySerialiser.ENTITY_JACKSON_REFERENCES);
            if (references == null) {
                context.put(EntitySerialiser.ENTITY_JACKSON_REFERENCES, references = new References());
            } else {
                references.reset();
            }
            context.putTemp(EntitySerialiser.ENTITY_JACKSON_REFERENCES, references);
        }
        final String reference = references.getReference(entity);
        if (reference != null) {
            generator.writeStartObject();

            generator.writeFieldName("@id_ref");
            generator.writeObject(reference);

            generator.writeEndObject();
        } else {
            final String newReference = EntitySerialiser.newSerialisationId(entity, references, typeNumber());
            references.putReference(entity, newReference);

            generator.writeStartObject();

            generator.writeFieldName("@id");
            generator.writeObject(newReference);

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

                if (value != null || !excludeNulls) {
                    // write actual property
                    generator.writeFieldName(name);
                    generator.writeObject(value);
                    // write actual meta-property
                    generator.writeFieldName("@" + name);
                    generator.writeStartObject();

                    final MetaProperty<Object> metaProperty = entity.getProperty(name);
                    if (!defaultValueContract.isEditableDefault(metaProperty)) {
                        generator.writeFieldName("_" + MetaProperty.EDITABLE_PROPERTY_NAME);
                        generator.writeObject(defaultValueContract.getEditable(metaProperty));
                    }
                    if (!defaultValueContract.isDirtyDefault(metaProperty)) {
                        generator.writeFieldName("_dirty");
                        generator.writeObject(defaultValueContract.getDirty(metaProperty));
                    }
                    if (!defaultValueContract.isRequiredDefault(metaProperty)) {
                        generator.writeFieldName("_" + MetaProperty.REQUIRED_PROPERTY_NAME);
                        generator.writeObject(defaultValueContract.getRequired(metaProperty));
                    }
                    if (!defaultValueContract.isVisibleDefault(metaProperty)) {
                        generator.writeFieldName("_visible");
                        generator.writeObject(defaultValueContract.getVisible(metaProperty));
                    }
                    if (!defaultValueContract.isValidationResultDefault(metaProperty)) {
                        generator.writeFieldName("_validationResult");
                        generator.writeObject(defaultValueContract.getValidationResult(metaProperty));
                    }

                    generator.writeEndObject();
                }
            }

            generator.writeEndObject();
        }
    }

    private Long typeNumber() {
        return entityTypeInfo.getKey().equals(EntityTypeInfo.class.getName()) ? 0L : entityTypeInfo.get_number();
    }
}
