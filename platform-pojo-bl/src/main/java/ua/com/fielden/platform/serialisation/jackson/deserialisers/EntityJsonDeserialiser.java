package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getEditableDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getRequiredDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getValueChangeCountDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getVisibleDefault;
import static ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.ID_ONLY_PROXY_PREFIX;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntityDeserialisationException;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Standard Jackson deserialiser for TG entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class EntityJsonDeserialiser<T extends AbstractEntity<?>> extends StdDeserializer<T> {
    private static final long serialVersionUID = 1L;
    private final EntityFactory factory;
    private final ObjectMapper mapper;
    private final Field versionField;
    private final Class<T> type;
    private final List<CachedProperty> properties;
    private final ISerialisationTypeEncoder serialisationTypeEncoder;
    private final boolean propertyDescriptorType;
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    public EntityJsonDeserialiser(final ObjectMapper mapper, final EntityFactory entityFactory, final Class<T> type, final List<CachedProperty> properties, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache, final boolean propertyDescriptorType) {
        super(type);
        this.factory = entityFactory;
        this.mapper = mapper;
        this.properties = properties;
        this.serialisationTypeEncoder = serialisationTypeEncoder;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;

        this.type = type;
        versionField = Finder.findFieldByName(type, AbstractEntity.VERSION);
        versionField.setAccessible(true);
        
        this.propertyDescriptorType = propertyDescriptorType;
    }

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
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

        final JsonNode node = jp.readValueAsTree();
        // final int reference = IntSerializer.get(buffer, true);

        if (node.isObject() && node.get("@id_ref") != null) {
            final String reference = node.get("@id_ref").asText();
            return (T) references.getEntity(reference);
        } else {
            //            // deserialise type
            //            final Class<T> type = (Class<T>) findClass(node.get("@entityType").asText());

            // deserialise id
            final JsonNode idJsonNode = node.get(AbstractEntity.ID); // the node should not be null itself
            final Long id = idJsonNode.isNull() ? null : idJsonNode.asLong();
            
            final JsonNode uninstrumentedJsonNode = node.get("@uninstrumented");
            final boolean uninstrumented = uninstrumentedJsonNode != null;

            final String[] proxiedProps = properties.stream()
                    .map(cachedProp -> cachedProp.name)
                    .filter(prop -> node.get(prop) == null)
                    .collect(toList())
                    .toArray(new String[] {});
            final T entity;
            // Property Descriptor: key and desc properties of propDescriptor are set through setters, not through fields; avoid validators on these properties or otherwise isInitialising:=true would be needed here
            if (uninstrumented) {
                if (propertyDescriptorType) {
                    entity = (T) PropertyDescriptor.fromString(node.get("@pdString").asText());
                } else {
                    entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(type, proxiedProps), id);
                }
            } else {
                if (propertyDescriptorType) {
                    entity = (T) PropertyDescriptor.fromString(node.get("@pdString").asText(), Optional.of(factory));
                } else {
                    entity = factory.newEntity(EntityProxyContainer.proxy(type, proxiedProps), id);
                }
            }
            final JsonNode atIdNode = node.get("@id");
            // At this stage 'clientSideReference' has been already decoded using ISerialisationTypeEncoder, that is why concrete EntityJsonDeserialiser has been chosen for deserialisation
            // Method determineValue is doing the necessary type determination with the usage of TgJackson.extractConcreteType method.
            final String clientSideReference = atIdNode == null ? null : atIdNode.asText();
            references.putEntity(clientSideReference, entity);

            // deserialise version -- should never be null
            final JsonNode versionJsonNode = node.get(AbstractEntity.VERSION); // the node should not be null itself
            //            if (versionJsonNode.isNull()) {
            //                throw new IllegalStateException("EntitySerialiser has got null 'version' property when reading entity of type [" + type.getName() + "].");
            //            }
            if (!versionJsonNode.isNull()) {
                final Long version = versionJsonNode.asLong();
                try {
                    versionField.set(entity, version); // at this stage the field should be already accessible
                } catch (final IllegalAccessException ex) {
                    throw new EntityDeserialisationException("The field [" + versionField + "] is not accessible. Fatal error during deserialisation process for entity [" + entity + "].", ex);
                } catch (final IllegalArgumentException ex) {
                    throw new EntityDeserialisationException("The field [" + versionField + "] is not declared in entity with type [" + type.getName() + "]. Fatal error during deserialisation process for entity [" + entity + "].", ex);
                }
            }

            final List<CachedProperty> nonProxiedProps = properties.stream().filter(prop -> node.get(prop.name) != null).collect(Collectors.toList()); 
            for (final CachedProperty prop : nonProxiedProps) {
                final String propName = prop.name;
                final Field propField = Finder.findFieldByName(entity.getClass(), propName);
                final JsonNode propNode = node.get(propName);
                final Object value = determineValue(propNode, propField);
                try {
                    prop.assignValue(entity, value); // at this stage the field should be already accessible
                } catch (final IllegalAccessException ex) {
                    throw new EntityDeserialisationException("The field [" + propName + "] is not accessible. Fatal error during deserialisation process for entity [" + entity + "].", ex);
                }
                entity.getPropertyOptionally(propName).ifPresent(metaProperty -> 
                    deserialiseMetaProperty((MetaProperty<Object>) metaProperty, node.get("@" + propName), propField)
                );
            }
            return entity;
        }
    }
    
    /**
     * Deserialises value from <code>valueNode</code> considering relation to <code>propertyField</code> as the field for property (which contains, for example, type information).
     * <p>
     * This method works also for id-only-proxy values.
     * 
     * @param valueNode
     * @param propertyField
     * @return
     */
    private Object determineValue(final JsonNode valueNode, final Field propertyField) {
        final Object value;
        if (valueNode.isNull()) {
            value = null;
        } else {
            final JavaType concreteType = concreteTypeOf(constructType(mapper.getTypeFactory(), propertyField), () -> valueNode.get("@id") == null ? valueNode.get("@id_ref") : valueNode.get("@id"));
            if (valueNode.isTextual() && EntityUtils.isEntityType(concreteType.getRawClass()) && valueNode.asText().startsWith(ID_ONLY_PROXY_PREFIX)) { // id-only proxy instance is represented as id-only proxy prefix concatenated with id number
                final Long determinedId = Long.valueOf(valueNode.asText().replaceFirst(Pattern.quote(ID_ONLY_PROXY_PREFIX), ""));
                value = EntityFactory.newPlainEntity(idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor((Class) concreteType.getRawClass()), determinedId);
            } else {
                try {
                    value = mapper.readValue(valueNode.traverse(mapper), concreteType);
                } catch (final IOException ex) {
                    throw new EntityDeserialisationException("Validation result deserialisation failed.", ex);
                }
            }
        }
        return value;
    }

    /**
     * Extracts concrete type for property based on constructed type (perhaps abstract).
     *
     * @param constructedType
     * @param idNodeSupplier
     * @return
     */
    private JavaType concreteTypeOf(final ResolvedType constructedType, final Supplier<JsonNode> idNodeSupplier) {
        return TgJackson.extractConcreteType(constructedType, idNodeSupplier, mapper.getTypeFactory(), serialisationTypeEncoder);
    }

    /**
     * Deserialises meta-property with all information that is relevant for entity lifecycle: 'validationResult', 
     * 'originalValue', 'dirty', 'prevValue' / 'lastInvalidValue', 'valueChangeCount', 'assigned', 'editable', 'required' and 'visible'.
     * <p>
     * Regarding original value: in case of non-existence of 'changedFromOriginal' information, originalValue and dirtiness must be reset by {@link MetaProperty#resetState()} method.
     * <p>
     * Please note that imperative 'dirty' flag on meta property sets to the value which is equal to 'changedFromOriginal', however
     * the 'dirty' flag in target entity could be different from 'changedFromOriginal'.
     *
     * @param metaProperty
     * @param metaPropNode
     * @param propField
     * 
     * @return
     */
    private MetaProperty<Object> deserialiseMetaProperty(final MetaProperty<Object> metaProperty, final JsonNode metaPropNode, final Field propField) {
        // deserialise validation result
        if (metaPropNode != null && metaPropNode.isObject() && metaPropNode.size() > 0) {
            final JsonNode validationResultNode = metaPropNode.get("_validationResult");
            if (validationResultNode != null) {
                assertNonEmptyNode(validationResultNode);
                try {
                    metaProperty.setDomainValidationResult(validationResultNode.traverse(mapper).readValueAs(Result.class));
                } catch (final IOException ex) {
                    throw new EntityDeserialisationException("Validation result deserialisation failed.", ex);
                }
            }
        }
        
        // deserialise original value and dirtiness
        if (metaPropNode == null || metaPropNode.get("_cfo") == null) {
            // do nothing -- there is no node and that means that there is default value
            metaProperty.resetState();
        } else {
            final JsonNode changedFromOriginalNode = metaPropNode.get("_cfo");
            assertNonEmptyNode(changedFromOriginalNode);
            final boolean changedFromOriginalValue = changedFromOriginalNode.asBoolean();
            
            if (changedFromOriginalValue) {
                final JsonNode originalValNode = metaPropNode.get("_originalVal");
                final Object originalVal = determineValue(originalValNode, propField);
                metaProperty.setOriginalValue(originalVal);
                
                metaProperty.setDirty(changedFromOriginalValue);
            } else {
                metaProperty.resetState();
            }
        }
        
        // 'originalVal' setting triggers updating of 'prevValue', 'valueChangeCount' and 'assigned'.
        // These three items (and 'lastInvalidValue' too) need to be properly assigned from deserialisation envelope.
        // 'setOriginalValue' call resets these three items.
        // Thats why custom assignments must be done AFTER 'setOriginalValue' call.
        // Also these assignments must be done BEFORE any logic that uses them.
        // One of examples is 'setRequired' call that uses 'getLastAttemptedValue' concept which is heavily based on validation results, 'assigned', value / originalValue and 'lastInvalidValue'.
        if (metaPropNode != null && metaPropNode.isObject() && metaPropNode.size() > 0) {
            final JsonNode prevValueNode = metaPropNode.get("_prevValue");
            if (prevValueNode != null) {
                metaProperty.setPrevValue(determineValue(prevValueNode, propField));
            }
            final JsonNode lastInvalidValueNode = metaPropNode.get("_lastInvalidValue");
            if (lastInvalidValueNode != null) {
                metaProperty.setLastInvalidValue(determineValue(lastInvalidValueNode, propField));
            }
            final JsonNode valueChangeCountNode = metaPropNode.get("_valueChangeCount");
            if (valueChangeCountNode != null) {
                assertNonEmptyNode(valueChangeCountNode);
                metaProperty.setValueChangeCount(valueChangeCountNode.intValue());
            } else {
                metaProperty.setValueChangeCount(getValueChangeCountDefault());
            }
            final JsonNode assignedNode = metaPropNode.get("_assigned");
            assertNonEmptyNode(assignedNode);
            metaProperty.setAssigned(assignedNode.asBoolean());
                
            final JsonNode editableNode = metaPropNode.get("_" + MetaProperty.EDITABLE_PROPERTY_NAME);
            if (editableNode != null) {
                assertNonEmptyNode(editableNode);
                metaProperty.setEditable(editableNode.asBoolean());
            } else {
                metaProperty.setEditable(getEditableDefault());
            }
            final JsonNode requiredNode = metaPropNode.get("_" + MetaProperty.REQUIRED_PROPERTY_NAME);
            if (requiredNode != null) {
                assertNonEmptyNode(requiredNode);
                // Important: generally there is no need to hold 'entity' in isInitialising state during deserialisation.
                // However in specific case of requiredness setting the setter can be invoked, that's why validation should be avoided -- isInitialising == true helps with that.
                metaProperty.getEntity().beginInitialising();
                metaProperty.setRequired(requiredNode.asBoolean());
                metaProperty.getEntity().endInitialising();
            } else {
                metaProperty.getEntity().beginInitialising();
                metaProperty.setRequired(getRequiredDefault());
                metaProperty.getEntity().endInitialising();
            }
            final JsonNode visibleNode = metaPropNode.get("_visible");
            if (visibleNode != null) {
                assertNonEmptyNode(visibleNode);
                metaProperty.setVisible(visibleNode.asBoolean());
            } else {
                metaProperty.setVisible(getVisibleDefault());
            }
        }
        return metaProperty;
    }
    
    /**
     * Asserts node on non-emptiness.
     * 
     * @param node
     */
    private static void assertNonEmptyNode(final JsonNode node) {
        if (node == null) {
            throw new EntityDeserialisationException("EntitySerialiser has got no node during meta property deserialisation.");
        }
        if (node.isNull()) {
            throw new EntityDeserialisationException("EntitySerialiser has got 'null' node inside during meta property deserialisation.");
        }
    }
    
    private ResolvedType constructType(final TypeFactory typeFactory, final Field propertyField) {
        final Class<?> fieldType = AbstractEntity.KEY.equals(propertyField.getName()) ? AnnotationReflector.getKeyType(type) : PropertyTypeDeterminator.stripIfNeeded(propertyField.getType());
        if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
            final ParameterizedType paramType = (ParameterizedType) propertyField.getGenericType();
            final Class<?> elementClass = PropertyTypeDeterminator.classFrom(paramType.getActualTypeArguments()[0]);

            return typeFactory.constructCollectionType((Class<? extends Collection>) fieldType, elementClass);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            final ParameterizedType paramType = (ParameterizedType) propertyField.getGenericType();
            // IMPORTANT: only simple Java types are supported for map keys (see http://stackoverflow.com/questions/6371092/can-not-find-a-map-key-deserializer-for-type-simple-type-class-com-comcast-i)
            final Class<?> keyClass = PropertyTypeDeterminator.classFrom(paramType.getActualTypeArguments()[0]);
            final Class<?> valueClass = PropertyTypeDeterminator.classFrom(paramType.getActualTypeArguments()[1]);

            return typeFactory.constructMapType((Class<? extends Map>) fieldType, keyClass, valueClass);
        } else {
            // TODO no other collectional types are supported at this stage -- should be added one by one
            return typeFactory.constructType(fieldType);
        }
    }
}
