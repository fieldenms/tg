package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;

public class EntityJsonDeserialiser<T extends AbstractEntity<?>> extends StdDeserializer<T> {
    private static final long serialVersionUID = 1L;
    private final EntityFactory factory;
    private final ObjectMapper mapper;
    private final Field versionField;
    private final Class<T> type;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<CachedProperty> properties;
    private final EntityType entityType;
    private final EntityTypeInfoGetter entityTypeInfoGetter;

    public EntityJsonDeserialiser(final ObjectMapper mapper, final EntityFactory entityFactory, final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final EntityTypeInfoGetter entityTypeInfoGetter) {
        super(type);
        this.factory = entityFactory;
        this.mapper = mapper;
        this.properties = properties;
        this.entityType = entityType;
        this.entityTypeInfoGetter = entityTypeInfoGetter;

        this.type = type;
        versionField = Finder.findFieldByName(type, AbstractEntity.VERSION);
        versionField.setAccessible(true);
    }

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
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

            final T entity;
            if (uninstrumented) {
                entity = factory.newPlainEntity(type, id);
                entity.setEntityFactory(factory);
            } else {
                entity = factory.newEntity(type, id);
            }

            final String newReference = EntitySerialiser.newSerialisationId(entity, references, entityType.get_number());
            references.putEntity(newReference, entity);

            // deserialise version -- should never be null
            final JsonNode versionJsonNode = node.get(AbstractEntity.VERSION); // the node should not be null itself
            //            if (versionJsonNode.isNull()) {
            //                throw new IllegalStateException("EntitySerialiser has got null 'version' property when reading entity of type [" + type.getName() + "].");
            //            }
            if (!versionJsonNode.isNull()) {
                final Long version = versionJsonNode.asLong();

                try {
                    // at this stage the field should be already accessible
                    versionField.set(entity, version);
                } catch (final IllegalAccessException e) {
                    // developer error -- please ensure that all fields are accessible
                    e.printStackTrace();
                    logger.error("The field [" + versionField + "] is not accessible. Fatal error during deserialisation process for entity [" + entity + "].", e);
                    throw new RuntimeException(e);
                } catch (final IllegalArgumentException e) {
                    e.printStackTrace();
                    logger.error("The field [" + versionField + "] is not declared in entity with type [" + type.getName() + "]. Fatal error during deserialisation process for entity [" + entity + "].", e);
                    throw e;
                }
            }

            //      entity.setInitialising(true);
            for (final CachedProperty prop : properties) {
                final String propertyName = prop.field().getName();
                final JsonNode propNode = node.get(propertyName);
                if (propNode != null) {
                    final Object value = determineValue(propNode, prop.field());
                    if (value != null) {
                        try {
                            // at this stage the field should be already accessible
                            prop.field().set(entity, value);
                        } catch (final IllegalAccessException e) {
                            // developer error -- please ensure that all fields are accessible
                            e.printStackTrace();
                            logger.error("The field [" + prop.field() + "] is not accessible. Fatal error during deserialisation process for entity [" + entity + "].", e);
                            throw new RuntimeException(e);
                        } catch (final IllegalArgumentException e) {
                            e.printStackTrace();
                            logger.error("The field [" + prop.field() + "] is not declared in entity with type [" + type.getName() + "]. Fatal error during deserialisation process for entity [" + entity + "].", e);
                            throw e;
                        }
                    }
                    if (!uninstrumented) {
                        // this is very important -- original values for non-persistent entities should be left 'null'!
                        final Object originalValue = entity.isPersisted() ? value : null;
                        if (entity.isPersisted()) {
                            entity.getProperty(propertyName).setOriginalValue(originalValue);
                        }
                    }
                }
                final JsonNode metaPropNode = node.get("@" + propertyName);
                if (metaPropNode != null) {
                    if (metaPropNode.isNull()) {
                        throw new IllegalStateException("EntitySerialiser has got null meta property '@" + propertyName + "' when reading entity of type [" + type.getName() + "].");
                    }
                    final MetaProperty metaProperty = entity.getProperty(propertyName);
                    provideChangedFromOriginal(metaProperty, metaPropNode);
                    provideEditable(metaProperty, metaPropNode);
                    provideRequired(metaProperty, metaPropNode);
                    provideVisible(metaProperty, metaPropNode);
                }
            }
            //      entity.setInitialising(false);

            return entity;
        }
    }

    private Object determineValue(final JsonNode propNode, final Field propertyField) throws IOException, JsonMappingException, JsonParseException {
        final Object value;
        if (propNode.isNull()) {
            value = null;
        } else {
            value = mapper.readValue(propNode.traverse(mapper), concreteTypeOf(constructType(mapper.getTypeFactory(), propertyField), () -> {
                return propNode.get("@id") == null ? propNode.get("@id_ref") : propNode.get("@id");
            }));
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
        return TgJackson.extractConcreteType(constructedType, idNodeSupplier, entityTypeInfoGetter, mapper.getTypeFactory());
    }

    /**
     * Retrieves 'dirty' value from entity JSON tree.
     *
     * @param metaProperty
     * @param metaPropNode
     * @return
     */
    private void provideChangedFromOriginal(final MetaProperty metaProperty, final JsonNode metaPropNode) {
        final JsonNode changedPropNode = metaPropNode.get("_cfo");
        if (changedPropNode == null) {
            // do nothing -- there is no node and that means that there is default value
        } else {
            if (changedPropNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'changedFromOriginal' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
            }
            if (metaProperty != null) {
                metaProperty.setDirty(changedPropNode.asBoolean());
            }
        }
    }

    /**
     * Retrieves 'editable' value from entity JSON tree.
     *
     * @param metaProperty
     * @param metaPropNode
     * @return
     */
    private void provideEditable(final MetaProperty metaProperty, final JsonNode metaPropNode) {
        final JsonNode editablePropNode = metaPropNode.get("_" + MetaProperty.EDITABLE_PROPERTY_NAME);
        if (editablePropNode == null) {
            // do nothing -- there is no node and that means that there is default value
        } else {
            if (editablePropNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'editable' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
            }
            if (metaProperty != null) {
                metaProperty.setEditable(editablePropNode.asBoolean());
            }
        }
    }

    /**
     * Retrieves 'required' value from entity JSON tree.
     *
     * @param metaProperty
     * @param metaPropNode
     * @return
     */
    private void provideRequired(final MetaProperty metaProperty, final JsonNode metaPropNode) {
        final JsonNode requiredPropNode = metaPropNode.get("_" + MetaProperty.REQUIRED_PROPERTY_NAME);
        if (requiredPropNode == null) {
            // do nothing -- there is no node and that means that there is default value
        } else {
            if (requiredPropNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'required' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
            }
            if (metaProperty != null) {
                metaProperty.setRequired(requiredPropNode.asBoolean());
            }
        }
    }

    /**
     * Retrieves 'visible' value from entity JSON tree.
     *
     * @param metaProperty
     * @param metaPropNode
     * @return
     */
    private void provideVisible(final MetaProperty metaProperty, final JsonNode metaPropNode) {
        final JsonNode visiblePropNode = metaPropNode.get("_visible");
        if (visiblePropNode == null) {
            // do nothing -- there is no node and that means that there is default value
        } else {
            if (visiblePropNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'visible' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
            }
            if (metaProperty != null) {
                metaProperty.setVisible(visiblePropNode.asBoolean());
            }
        }
    }

    //    /**
    //     * Retrieves 'ValidationResult' value from entity JSON tree.
    //     *
    //     * @param metaProperty
    //     * @param metaPropNode
    //     * @return
    //     */
    //    private void provideValidationResult(final MetaProperty metaProperty, final JsonNode metaPropNode) throws IOException, JsonProcessingException {
    //        final JsonNode validationResultPropNode = metaPropNode.get("_validationResult");
    //        if (validationResultPropNode == null) {
    //            // do nothing -- there is no node and that means that there is default value
    //        } else {
    //            if (validationResultPropNode.isNull()) {
    //                throw new IllegalStateException("EntitySerialiser has got null 'ValidationResult' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
    //            }
    //            if (metaProperty != null) {
    //                final JsonParser jsonParser = validationResultPropNode.traverse(mapper);
    //                metaProperty.setRequiredValidationResult(jsonParser.readValueAs(Result.class)); // TODO how can it be done for Warning.class??
    //            }
    //        }
    //    }

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
