package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
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
import ua.com.fielden.platform.serialisation.jackson.PropertyDeserialisationErrorHandler;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntityDeserialisationException;
import ua.com.fielden.platform.utils.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.fromString;
import static ua.com.fielden.platform.entity.proxy.EntityProxyContainer.proxy;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.*;
import static ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.ID_ONLY_PROXY_PREFIX;
import static ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser.VALIDATION_RESULT;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.entityWithMocksFromString;

/// Standard Jackson deserialiser for TG entities.
///
public class EntityJsonDeserialiser<T extends AbstractEntity<?>> extends StdDeserializer<T> {

    public static final String
            ERR_INVALID_SERIALISED_ENTITY = "Invalid serialised entity: must be an object node, but was %s.",
            ERR_COULD_NOT_MODIFY_FIELD = "Could not modify field [%s] in %s [%s].",
            ERR_FAILED_TO_DESERIALISE_PROPERTY = "Failed to deserialise property [%s.%s].",
            ERR_DESERIALISING_VALIDATION_RESULT = "Failed to deserialise domain validation result for meta-property [%s.%s].",
            ERR_SERIALISER_HAS_NO_NODE = "EntitySerialiser has got no node during meta property deserialisation.",
            ERR_SERIALISER_HAS_NULL_NODE = "EntitySerialiser has got 'null' node inside during meta property deserialisation.";

    private static final Function<CachedProperty, String> FIELD_NAME = prop -> prop.field().getName(); // create once for all EntityJsonDeserialiser instances
    private static final Function<CachedProperty, CachedProperty> IDENTITY = identity(); // create once for all EntityJsonDeserialiser instances
    private static final BinaryOperator<CachedProperty> TAKE_SECOND = (prop1, prop2) -> prop2; // create once for all EntityJsonDeserialiser instances
    private final EntityFactory factory;
    private final ObjectMapper mapper;
    private final Field versionField;
    private final Class<T> entityType;
    private final Map<String, CachedProperty> properties;
    private final ISerialisationTypeEncoder serialisationTypeEncoder;
    private final boolean propertyDescriptorType;
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    public EntityJsonDeserialiser(
            final ObjectMapper mapper,
            final EntityFactory entityFactory,
            final Class<T> entityType,
            final List<CachedProperty> properties,
            final ISerialisationTypeEncoder serialisationTypeEncoder,
            final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache,
            final boolean propertyDescriptorType)
    {
        super(entityType);
        this.factory = entityFactory;
        this.mapper = mapper;
        this.properties = properties.stream().collect(toMap(
            FIELD_NAME, // collect to map by field name
            IDENTITY,
            TAKE_SECOND, // very unlikely to have duplicates here (see un-overridden CachedProperty.equals and EntitySerialiser.createCachedProperties); however, for safety, do not throw IllegalStateException as in Collectors.throwingMerger (used by Collectors.toMap(keyMapper, valueMapper) method)
            LinkedHashMap::new // preserve iteration order through this map exactly as in original List<CachedProperty>
        ));
        this.serialisationTypeEncoder = serialisationTypeEncoder;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;

        this.entityType = entityType;
        versionField = Finder.findFieldByName(entityType, AbstractEntity.VERSION);
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

        if (!node.isObject()) {
            throw new EntityDeserialisationException(ERR_INVALID_SERIALISED_ENTITY.formatted(node.getNodeType()));
        }

        if (node.get("@id_ref") != null) {
            final String reference = node.get("@id_ref").asText();
            return (T) references.getEntity(reference);
        } else {
            // deserialise id
            final JsonNode idJsonNode = node.get(AbstractEntity.ID); // the node should not be null itself
            final Long id = idJsonNode.isNull() ? null : idJsonNode.asLong();

            final JsonNode instrumentedJsonNode = node.get("@_i");
            final boolean uninstrumented = instrumentedJsonNode == null;

            final Set<String> proxiedProps = properties.keySet().stream()
                    .filter(prop -> node.get(prop) == null)
                    .collect(toImmutableSet());

            final T entity;
            // Property Descriptor: key and desc properties of propDescriptor are set through setters, not through fields; avoid validators on these properties or otherwise isInitialising:=true would be needed here
            if (propertyDescriptorType) {
                entity = entityWithMocksFromString(str -> (T) (uninstrumented ? fromString(str) : fromString(str, of(factory))), node.get("@pdString").asText(), entityType);
            } else {
                entity = uninstrumented ? newPlainEntity(proxy(entityType, proxiedProps), id) : factory.newEntity(proxy(entityType, proxiedProps), id);
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
                } catch (final Exception ex) {
                    throw new EntityDeserialisationException(ERR_COULD_NOT_MODIFY_FIELD.formatted(versionField.getName(), entity.getType().getCanonicalName(), entity), ex);
                }
            }

            ofNullable(node.get("@_pp")) // for undefined node, leave preferred property 'null' (default value after creation)
                .filter(JsonNode::isTextual) // for non-textual value, also leave preferred property 'null' (e.g. in case of client-side erroneous entity's preferred property manipulation)
                .map(JsonNode::asText) // for defined node, set its textual representation
                .ifPresent(entity::setPreferredProperty);

            final var propErrorHandler = context.getPropDeserialisationErrorHandler().orElse(PropertyDeserialisationErrorHandler.standard);
            node.properties().forEach(childNameAndNode -> { // iterate over all "fields" (i.e., present child nodes) in the order of the original source
                final String childName = childNameAndNode.getKey();
                final JsonNode childNode = childNameAndNode.getValue();
                if (childNode != null) { // for safety, still check whether JsonNode is present
                    final CachedProperty prop = properties.get(childName);
                    if (prop != null) { // only consider child nodes present in the CachedProperty map (i.e., properties defined in the entity type)
                        try {
                            final Object value = determineValue(childNode, prop.field());
                            try {
                                prop.field().set(entity, value); // at this stage the field should be already accessible
                            } catch (final Exception ex) {
                                throw new EntityDeserialisationException(ERR_COULD_NOT_MODIFY_FIELD.formatted(versionField.getName(), entity.getType().getCanonicalName(), entity), ex);
                            }
                            final Optional<MetaProperty<?>> metaPropertyOpt = entity.getPropertyOptionally(childName);
                            metaPropertyOpt.ifPresent(metaProperty -> deserialiseMetaProperty((MetaProperty<Object>) metaProperty, node.get("@" + childName), prop.field()));
                        } catch (final RuntimeException ex) {
                            propErrorHandler.handle(entity, prop.field().getName(), childNode::toString, ex);
                        }
                    }
                }
            });
            return entity;
        }
    }
    
    /// Deserialises value from `valueNode` considering its relation to `propertyField` as the field for property, which contains, for example, type information.
    ///
    /// This method works also for id-only-proxy values.
    ///
    private Object determineValue(final JsonNode valueNode, final Field propertyField) {
        final Object value;
        if (valueNode.isNull()) {
            value = null;
        } else {
            final JavaType concreteType = concreteTypeOf(constructType(entityType, mapper.getTypeFactory(), propertyField), () -> valueNode.get("@id") == null ? valueNode.get("@id_ref") : valueNode.get("@id"));
            if (valueNode.isTextual() && EntityUtils.isEntityType(concreteType.getRawClass()) && valueNode.asText().startsWith(ID_ONLY_PROXY_PREFIX)) { // id-only proxy instance is represented as id-only proxy prefix concatenated with id number
                final Long determinedId = Long.valueOf(valueNode.asText().replaceFirst(Pattern.quote(ID_ONLY_PROXY_PREFIX), ""));
                value = EntityFactory.newPlainEntity(idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor((Class) concreteType.getRawClass()), determinedId);
            } else {
                try {
                    value = mapper.readValue(valueNode.traverse(mapper), concreteType);
                } catch (final Exception ex) {
                    throw new EntityDeserialisationException(
                            ERR_FAILED_TO_DESERIALISE_PROPERTY.formatted( propertyField.getDeclaringClass().getTypeName(), propertyField.getName()),
                            ex);
                }
            }
        }
        return value;
    }

    /// Extracts concrete type for property based on constructed type (perhaps abstract).
    ///
    private JavaType concreteTypeOf(final ResolvedType constructedType, final Supplier<JsonNode> idNodeSupplier) {
        return TgJackson.extractConcreteType(constructedType, idNodeSupplier, mapper.getTypeFactory(), serialisationTypeEncoder);
    }

    /// Deserialises meta-property with all information that is relevant for entity lifecycle:
    /// - `validationResult`,
    /// - `originalValue`,
    /// - `dirty`,
    /// - `prevValue` / `lastInvalidValue`,
    /// - `valueChangeCount`,
    /// - `assigned`,
    /// - `editable`,
    /// - `required`, and
    /// - `visible`.
    ///
    /// Regarding the original value, in case of non-existence of the `changedFromOriginal` information, the `originalValue` and dirtiness must be reset by [#resetState()] method.
    ///
    /// Please note that imperative the `dirty` flag for a meta-property, sets to the value, which is equal to `changedFromOriginal`.
    /// However, the `dirty` flag in the target entity could be different to that of `changedFromOriginal`.
    ///
    private MetaProperty<Object> deserialiseMetaProperty(final MetaProperty<Object> metaProperty, final JsonNode metaPropNode, final Field propField) {
        // deserialise validation result
        if (metaPropNode != null && metaPropNode.isObject() && !metaPropNode.isEmpty()) {
            final JsonNode validationResultNode = metaPropNode.get(VALIDATION_RESULT);
            if (validationResultNode != null) {
                assertNonEmptyNode(validationResultNode);
                try (final var parser = validationResultNode.traverse(mapper)) {
                    metaProperty.setDomainValidationResult(parser.readValueAs(Result.class));
                } catch (final Exception ex) {
                    throw new EntityDeserialisationException(
                            ERR_DESERIALISING_VALIDATION_RESULT.formatted(propField.getDeclaringClass().getTypeName(), propField.getName()),
                            ex);
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
        // That's why custom assignments must be done AFTER 'setOriginalValue' call.
        // Also, these assignments must be done BEFORE any logic that uses them.
        // One of examples is 'setRequired' call that uses 'getLastAttemptedValue' concept which is heavily based on validation results, 'assigned', value / originalValue and 'lastInvalidValue'.
        if (metaPropNode != null && metaPropNode.isObject() && !metaPropNode.isEmpty()) {
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
            // before we deal with setting requiredness, it is necessary to identify if there is a custom error message for requiredness
            final JsonNode customErrorMsgForRequirednessNode = metaPropNode.get("_" + MetaProperty.CUSTOM_ERR_MSG_FOR_REQUREDNESS_PROPERTY_NAME);
            final String customErrorMsgForRequiredness;
            if (customErrorMsgForRequirednessNode != null) {
                assertNonEmptyNode(customErrorMsgForRequirednessNode);
                customErrorMsgForRequiredness = customErrorMsgForRequirednessNode.asText();
            } else {
                customErrorMsgForRequiredness = null;
            }
            final JsonNode requiredNode = metaPropNode.get("_" + MetaProperty.REQUIRED_PROPERTY_NAME);
            if (requiredNode != null) {
                assertNonEmptyNode(requiredNode);
                // Important: generally there is no need to hold 'entity' in isInitialising state during deserialisation.
                // However, in specific case of requiredness setting the setter can be invoked, that's why validation should be avoided -- isInitialising == true helps with that.
                metaProperty.getEntity().beginInitialising();
                metaProperty.setRequired(requiredNode.asBoolean(), customErrorMsgForRequiredness);
                metaProperty.getEntity().endInitialising();
            } else {
                metaProperty.getEntity().beginInitialising();
                metaProperty.setRequired(getRequiredDefault(), customErrorMsgForRequiredness);
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
    
    /// Asserts node on non-emptiness.
    ///
    private static void assertNonEmptyNode(final JsonNode node) {
        if (node == null) {
            throw new EntityDeserialisationException(ERR_SERIALISER_HAS_NO_NODE);
        }
        if (node.isNull()) {
            throw new EntityDeserialisationException(ERR_SERIALISER_HAS_NULL_NODE);
        }
    }
    
    private static <T extends AbstractEntity<?>> ResolvedType constructType(final Class<T> type, final TypeFactory typeFactory, final Field propertyField) {
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
        } else if (EntityUtils.isEntityType(fieldType)) {
            return SimpleType.constructUnsafe(fieldType);
        } else {
            // TODO no other collectional types are supported at this stage -- should be added one by one
            return typeFactory.constructType(fieldType);
        }
    }

}
