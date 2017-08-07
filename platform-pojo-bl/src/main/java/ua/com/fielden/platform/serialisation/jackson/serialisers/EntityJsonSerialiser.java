package ua.com.fielden.platform.serialisation.jackson.serialisers;

import static java.lang.String.format;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getValidationResult;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isChangedFromOriginalDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isEditableDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isLastInvalidValueDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isMaxDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isMinDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isPrevValueDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isRequiredDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isValidationResultDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isValueChangeCountDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isVisibleDefault;
import static ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.ID_ONLY_PROXY_PREFIX;

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
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.utils.Pair;

public class EntityJsonSerialiser<T extends AbstractEntity<?>> extends StdSerializer<T> {
    private final Class<T> type;
    private static final Logger LOGGER = Logger.getLogger(EntityJsonSerialiser.class);
    private final List<CachedProperty> properties;
    private final EntityType entityType;
    private final boolean excludeNulls;
    private final boolean propertyDescriptorType;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final boolean excludeNulls, final boolean propertyDescriptorType) {
        super(type);

        this.type = type;
        this.properties = properties;
        this.entityType = entityType;
        this.excludeNulls = excludeNulls;
        this.propertyDescriptorType = propertyDescriptorType;
    }

    @Override
    public void serialize(final T entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        if (entityType.get_identifier() == null) {
            throw new SerialisationException(format("The identifier of the type [%s] should be populated to be ready for serialisation.", entityType));
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
            final String newReference = EntitySerialiser.newSerialisationId(entity, references, entityType.get_identifier());
            references.putReference(entity, newReference);

            generator.writeStartObject();

            generator.writeFieldName("@id");
            generator.writeObject(newReference);
            
            if (propertyDescriptorType) {
                final PropertyDescriptor<?> pd = (PropertyDescriptor<?>) entity;
                // write property descriptor toString() value to special '@pdString' field
                generator.writeFieldName("@pdString");
                generator.writeObject(pd.toString());
            }
            
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
            if (Reflector.isPropertyProxied(entity, AbstractEntity.VERSION)) {
                generator.writeObject(Long.valueOf(0L));
            } else {
                generator.writeObject(entity.getVersion());
            }
            
            // serialise all the properties relying on the fact that property sequence is consistent with order of fields in the class declaration
            for (final CachedProperty prop : properties) {
                // non-composite keys should be persisted by identifying their actual type
                final String name = prop.field().getName();
                if (!Reflector.isPropertyProxied(entity, name)) {
                    Object value = null;
                    try {
                        // at this stage the field should be already accessible
                        value = prop.field().get(entity);
                    } catch (final IllegalAccessException e) {
                        // developer error -- please ensure that all fields are accessible
                        final String msg = format("The field [%s] is not accessible. Fatal error during serialisation process for entity [%s].", prop.field(), entity);
                        LOGGER.error(msg, e);
                        throw new SerialisationException(msg, e);
                    } catch (final IllegalArgumentException e) {
                        LOGGER.error(format("The field [%s] is not declared in entity with type [%s]. Fatal error during serialisation process for entity [%s].", prop.field(), type.getName(), entity), e);
                        throw e;
                    }
                    
                    if (!disregardValueSerialisation(value, excludeNulls)) {
                        // write actual property
                        generator.writeFieldName(name);
                        if (value != null && isIdOnlyProxiedEntity(value, prop.isEntityTyped())) {
                            final AbstractEntity<?> idOnlyProxyEntity = (AbstractEntity<?>) value;
                            generator.writeObject(ID_ONLY_PROXY_PREFIX + idOnlyProxyEntity.getId());
                        } else {
                            generator.writeObject(value);
                        }
                        
                        if (!uninstrumented) {
                            final MetaProperty<Object> metaProperty = entity.getProperty(name);
                            if (metaProperty == null) {
                                throw new SerialisationException(format("Meta property [%s] does not exist for instrumented entity instance with type [%s].", name, entity.getClass().getSimpleName()));
                            }
                            final Map<String, Object> existingMetaProps = new LinkedHashMap<>();
                            if (!isEditableDefault(metaProperty)) {
                                existingMetaProps.put("_" + MetaProperty.EDITABLE_PROPERTY_NAME, metaProperty.isEditable());
                            }
                            if (!isChangedFromOriginalDefault(metaProperty)) {
                                existingMetaProps.put("_cfo", metaProperty.isChangedFromOriginal());
                                existingMetaProps.put("_originalVal", metaProperty.getOriginalValue());
                            }
                            if (!isRequiredDefault(metaProperty)) {
                                existingMetaProps.put("_" + MetaProperty.REQUIRED_PROPERTY_NAME, metaProperty.isRequired());
                            }
                            if (!isVisibleDefault(metaProperty)) {
                                existingMetaProps.put("_visible", metaProperty.isVisible());
                            }
                            if (!isValidationResultDefault(metaProperty)) {
                                existingMetaProps.put("_validationResult", getValidationResult(metaProperty));
                            }
                            final Pair<Integer, Integer> minMax = Reflector.extractValidationLimits(entity, name);
                            final Integer min = minMax.getKey();
                            final Integer max = minMax.getValue();
                            if (!isMinDefault(min)) {
                                existingMetaProps.put("_min", min);
                            }
                            if (!isMaxDefault(max)) {
                                existingMetaProps.put("_max", max);
                            }
                            if (!isPrevValueDefault(metaProperty)) {
                                existingMetaProps.put("_prevValue", metaProperty.getPrevValue());
                            }
                            if (!isLastInvalidValueDefault(metaProperty)) {
                                existingMetaProps.put("_lastInvalidValue", metaProperty.getLastInvalidValue());
                            }
                            if (!isValueChangeCountDefault(metaProperty)) {
                                existingMetaProps.put("_valueChangeCount", metaProperty.getValueChangeCount());
                            }
                            existingMetaProps.put("_assigned", metaProperty.isAssigned());
                            
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
    
    /**
     * Returns <code>true</code> in case when value serialisation should be skipped, <code>false</code> otherwise.
     * 
     * @param value
     * @param excludeNulls
     * @return
     */
    private static boolean disregardValueSerialisation(final Object value, final boolean excludeNulls) {
        return value == null && excludeNulls;
    }
    
    /**
     * Returns <code>true</code> in case where non-null <code>value</code> represents id-only entity proxy, <code>false</code> otherwise.
     * 
     * @param value
     * @param isEntityTyped -- indicates whether <code>value</code> is the value of entity-typed property, <code>false</code> otherwise
     * @return
     */
    private static boolean isIdOnlyProxiedEntity(final Object value, final boolean isEntityTyped) {
        return isEntityTyped && ((AbstractEntity<?>) value).isIdOnlyProxy();
    }
}
