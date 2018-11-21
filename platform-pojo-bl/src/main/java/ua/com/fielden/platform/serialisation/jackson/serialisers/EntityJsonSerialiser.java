package ua.com.fielden.platform.serialisation.jackson.serialisers;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.reflection.Reflector.extractValidationLimits;
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
import static ua.com.fielden.platform.utils.EntityUtils.isDecimal;
import static ua.com.fielden.platform.utils.EntityUtils.isInteger;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.NOT_FOUND_MOCK_PREFIX;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.isMockNotFoundEntity;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntitySerialisationException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Standard Jackson serialiser for TG entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class EntityJsonSerialiser<T extends AbstractEntity<?>> extends StdSerializer<T> {

    public static final String ERR_RESTRICTED_TYPE_SERIALISATION_DUE_TO_PROP_TYPE = "Type [%s] containst property [%s] that is not permitted for serialisation.";

    private final Class<T> type;
    private static final Logger LOGGER = Logger.getLogger(EntityJsonSerialiser.class);
    private final transient List<CachedProperty> properties;
    private final transient EntityType entityType;
    private final boolean excludeNulls;
    private final boolean propertyDescriptorType;

    public EntityJsonSerialiser(final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final boolean excludeNulls, final boolean propertyDescriptorType) {
        super(type);

        // let's do due diligence to restrict serialisation of not permitted types...
        if (UserSecret.class.isAssignableFrom(type)) {
            throw new EntitySerialisationException(format("Type [%s] is not permitted for serialisation.", type.getName()));
        }
        // or types that contain properties of not permitted for serialisation types
        final Consumer<? super CachedProperty> error = cp -> {throw new EntitySerialisationException(format(ERR_RESTRICTED_TYPE_SERIALISATION_DUE_TO_PROP_TYPE, type.getSimpleName(), cp.field().getName()));};
        properties.stream().filter(cp -> UserSecret.class.isAssignableFrom(cp.field().getType())).findFirst().ifPresent(error);
        
        this.type = type;
        this.properties = properties;
        this.entityType = entityType;
        this.excludeNulls = excludeNulls;
        this.propertyDescriptorType = propertyDescriptorType;
    }

    @Override
    public void serialize(final T entity, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
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
                if (isMockNotFoundEntity(entity)) {
                    generator.writeObject(NOT_FOUND_MOCK_PREFIX + entity.get(DESC));
                } else {
                    generator.writeObject(pd.toString());
                }
            }
            
            final boolean uninstrumented = !PropertyTypeDeterminator.isInstrumented(entity.getClass());
            if (uninstrumented) {
                generator.writeFieldName("@uninstrumented");
                generator.writeObject(null);
            }

            // serialise id
            generator.writeFieldName(AbstractEntity.ID);
            generator.writeObject(getIdSafely(entity));

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
                        generator.writeObject(valueObject(value, prop.isEntityTyped()));
                        
                        if (!uninstrumented) {
                            final MetaProperty<Object> metaProperty = entity.getProperty(name);
                            if (metaProperty == null) {
                                throw new SerialisationException(format("Meta property [%s] does not exist for instrumented entity instance with type [%s].", name, entity.getClass().getSimpleName()));
                            }
                            final Map<String, Object> existingMetaProps = new LinkedHashMap<>();
                            if (!isEditableDefault(metaProperty)) {
                                existingMetaProps.put("_" + MetaProperty.EDITABLE_PROPERTY_NAME, metaProperty.isEditable());
                            }
                            final T2<Boolean, Optional<T2<Long, Long>>> changedFromOriginalDefaultResult = isChangedFromOriginalDefault(metaProperty);
                            if (!changedFromOriginalDefaultResult._1) {
                                if (changedFromOriginalDefaultResult._2.isPresent()) {
                                    final T2<Long, Long> idOnlyProxyIds = changedFromOriginalDefaultResult._2.get();
                                    existingMetaProps.put("_cfo", !EntityUtils.equalsEx(idOnlyProxyIds._1, idOnlyProxyIds._2));
                                } else {
                                    existingMetaProps.put("_cfo", metaProperty.isChangedFromOriginal());
                                }
                                existingMetaProps.put("_originalVal", valueObject(metaProperty.getOriginalValue(), prop.isEntityTyped()));
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
                            final Integer min;
                            final Integer max;
                            if (prop.getPropertyType() != null && (
                                isInteger(prop.getPropertyType())
                                || isDecimal(prop.getPropertyType())
                                || isString(prop.getPropertyType())
                            )) { // check annotations @GreaterOrEqual and @Max only for integer / decimal / money and string properties to reduce performance hit (method retrieval through reflection is heavy operation here)
                                final Pair<Integer, Integer> minMax = extractValidationLimits(entity, name);
                                min = minMax.getKey();
                                max = minMax.getValue();
                            } else {
                                min = null;
                                max = null;
                            }
                            if (!isMinDefault(min)) {
                                existingMetaProps.put("_min", min);
                            }
                            if (!isMaxDefault(max)) {
                                existingMetaProps.put("_max", max);
                            }
                            if (!isPrevValueDefault(metaProperty)) {
                                existingMetaProps.put("_prevValue", valueObject(metaProperty.getPrevValue(), prop.isEntityTyped()));
                            }
                            if (!isLastInvalidValueDefault(metaProperty)) {
                                existingMetaProps.put("_lastInvalidValue", valueObject(metaProperty.getLastInvalidValue(), prop.isEntityTyped()));
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
     * Until implementation of issue #999, it was always safe to get ID.
     * However, for synthetic entities method {@code getId()} may get overridden to return something like {@code getComputedId()}, which in turn may 
     * represent a getter for a proxied property. Thus, such calls to overridden {@link getId()} may result in exception at runtime.
     * <p>
     * As an example, consider a situation with an Entity Centre that is based on a synthetic entity and has summary.
     * Normally, instance that represent "summary" values have ID equal to {@code null} -- calling {@code getId()} returns {@code null}.
     * But calling {@code getId()}, which is overridden will most definitely result in a runtime error due to proxied property {@code computedId}.
     * <p>
     * Perhaps this is not an ideal solution, but for practical consideration, let's assume that getting ID should never fail. Hence, this method.  
     *
     * @param entity
     * @return
     */
    private Long getIdSafely(final T entity) {
        try {
            return entity.getId();
        } catch (final Exception ex) {
            LOGGER.debug("Could not get ID.", ex);
            return null;
        }
    }

    /**
     * Returns an object to be used for serialisation that corresponds to <code>value</code>.
     * <p>
     * Handles id-only-proxy values using special notation.
     * 
     * @param value
     * @param isEntityTyped
     * @return
     */
    private final Object valueObject(final Object value, final boolean isEntityTyped) {
        if (value != null && isIdOnlyProxiedEntity(value, isEntityTyped)) {
            final AbstractEntity<?> idOnlyProxyEntity = (AbstractEntity<?>) value;
            return ID_ONLY_PROXY_PREFIX + idOnlyProxyEntity.getId();
        } else {
            return value;
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
