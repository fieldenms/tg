package ua.com.fielden.platform.serialisation.jackson.serialisers;

import static javassist.util.proxy.ProxyFactory.isProxyClass;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.utils.Pair;

public class EntityJsonSerialiser<T extends AbstractEntity<?>> extends StdSerializer<T> {
    private final Class<T> type;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<CachedProperty> properties;
    private final EntityType entityType;
    private final DefaultValueContract defaultValueContract;
    private final boolean excludeNulls;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final DefaultValueContract defaultValueContract, final boolean excludeNulls) {
        super(type);
        if (entityType.get_number() == null) {
            throw new IllegalStateException("The number of the type [" + entityType + "] should be populated to be ready for serialisation.");
        }

        this.type = type;
        this.properties = properties;
        this.entityType = entityType;
        this.defaultValueContract = defaultValueContract;
        this.excludeNulls = excludeNulls;
    }

    @Override
    public void serialize(final T entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        if (isProxyClass(entity.getClass())) {
            throw new IllegalArgumentException(String.format("Entity with type [%s], which is Javassist proxy, should not be serialised at all.", entity.getClass().getSimpleName()));
        }
        
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
            final String newReference = EntitySerialiser.newSerialisationId(entity, references, entityType.get_number());
            references.putReference(entity, newReference);

            generator.writeStartObject();

            generator.writeFieldName("@id");
            generator.writeObject(newReference);
            
            final boolean uninstrumented = !PropertyTypeDeterminator.isInstrumented(entity.getClass());
            if (uninstrumented) {
                generator.writeFieldName("@uninstrumented");
                generator.writeObject(null);
            }

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
                    final MetaProperty<Object> metaProperty = entity.getProperty(name);
                    if (uninstrumented) {
                        // write actual property
                        generator.writeFieldName(name);
                        generator.writeObject(value);
                    } else {
                        if (value != null) {
                            value.toString(); // try to blow proxy errors
                        }
                        if (metaProperty == null) {
                            throw new IllegalStateException(String.format("Meta property [%s] does not exist for instrumented entity instance with type [%s].", name, entity.getType().getSimpleName()));
                        }
                        
                        if (!metaProperty.isProxy()) {
                            // write actual property
                            generator.writeFieldName(name);
                            generator.writeObject(value);

                            final Map<String, Object> existingMetaProps = new LinkedHashMap<>();
                            if (!defaultValueContract.isEditableDefault(metaProperty)) {
                                existingMetaProps.put("_" + MetaProperty.EDITABLE_PROPERTY_NAME, defaultValueContract.getEditable(metaProperty));
                            }
                            if (!defaultValueContract.isChangedFromOriginalDefault(metaProperty)) {
                                existingMetaProps.put("_cfo", defaultValueContract.getChangedFromOriginal(metaProperty));
                                existingMetaProps.put("_originalVal", defaultValueContract.getOriginalValue(metaProperty));
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
                            final Pair<Integer, Integer> minMax = Reflector.extractValidationLimits(entity, name);
                            final Integer min = minMax.getKey();
                            final Integer max = minMax.getValue();
                            if (!defaultValueContract.isMinDefault(min)) {
                                existingMetaProps.put("_min", min);
                            }
                            if (!defaultValueContract.isMaxDefault(max)) {
                                existingMetaProps.put("_max", max);
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
                }
            }

            generator.writeEndObject();
        }
    }
}
