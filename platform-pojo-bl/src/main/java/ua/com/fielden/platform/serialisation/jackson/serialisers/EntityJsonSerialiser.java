package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeProp;
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
    private final EntityType entityType;
    private final DefaultValueContract defaultValueContract;
    private final boolean excludeNulls;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final DefaultValueContract defaultValueContract, final boolean excludeNulls) {
        super(type);
        this.type = type;
        this.properties = properties;
        this.entityType = entityType;
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

                    final MetaProperty<Object> metaProperty = entity.getProperty(name);
                    final Map<String, Object> existingMetaProps = new LinkedHashMap<>();
                    if (!defaultValueContract.isEditableDefault(metaProperty)) {
                        existingMetaProps.put("_" + MetaProperty.EDITABLE_PROPERTY_NAME, defaultValueContract.getEditable(metaProperty));
                    }
                    if (!defaultValueContract.isDirtyDefault(metaProperty)) {
                        existingMetaProps.put("_dirty", defaultValueContract.getDirty(metaProperty));
                    }
                    if (!defaultValueContract.isRequiredDefault(metaProperty)) {
                        existingMetaProps.put("_" + MetaProperty.REQUIRED_PROPERTY_NAME, defaultValueContract.getRequired(metaProperty));
                    }
                    if (!defaultValueContract.isVisibleDefault(metaProperty)) {
                        existingMetaProps.put("_visible", defaultValueContract.getVisible(metaProperty));
                    }
                    if (!defaultValueContract.isValidationResultDefault(metaProperty)) {
                        existingMetaProps.put("_validationResult", defaultValueContract.getValidationResult(metaProperty));
                    }
                    // write actual meta-property
                    if (!existingMetaProps.isEmpty()) {
                        generator.writeFieldName("@" + name);
                        generator.writeStartObject();
                        for (final Map.Entry<String, Object> nameAndVal : existingMetaProps.entrySet()) {
                            generator.writeFieldName(nameAndVal.getKey());
                            generator.writeObject(nameAndVal.getValue());
                        }
                        generator.writeEndObject();
                    }
                }
            }

            generator.writeEndObject();
        }
    }

    private Long typeNumber() {
        return entityType.getKey().equals(EntityType.class.getName()) ? 0L :
                entityType.getKey().equals(EntityTypeProp.class.getName()) ? 1L : entityType.get_number();
    }
}
