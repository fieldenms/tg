package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class EntityJsonDeserialiser<T extends AbstractEntity<?>> extends StdDeserializer<T> {
    private final EntityFactory factory;
    private final ObjectMapper mapper;
    private final Field versionField;
    private final Class<T> type;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<CachedProperty> properties;
    private final EntityType entityType;
    private final DefaultValueContract defaultValueContract;

    public EntityJsonDeserialiser(final ObjectMapper mapper, final EntityFactory entityFactory, final Class<T> type, final List<CachedProperty> properties, final EntityType entityType, final DefaultValueContract defaultValueContract) {
        super(type);
        this.factory = entityFactory;
        this.mapper = mapper;
        this.properties = properties;
        this.entityType = entityType;
        this.defaultValueContract = defaultValueContract;

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

            final T entity;
            if (DynamicEntityClassLoader.isEnhanced(type)) {
                entity = factory.newPlainEntity(type, id);
                entity.setEntityFactory(factory);
            } else {
                entity = factory.newEntity(type, id);
            }

            final String newReference = EntitySerialiser.newSerialisationId(entity, references, typeNumber());
            references.putEntity(newReference, entity);

            // deserialise version -- should never be null
            final JsonNode versionJsonNode = node.get(AbstractEntity.VERSION); // the node should not be null itself
            if (versionJsonNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'version' property when reading entity of type [" + type.getName() + "].");
            }
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

            //      entity.setInitialising(true);
            for (final CachedProperty prop : properties) {
                final JsonNode propNode = node.get(prop.field().getName());
                if (propNode.isNull()) {
                    if (!DynamicEntityClassLoader.isEnhanced(type)) {
                        entity.getProperty(prop.field().getName()).setOriginalValue(null);
                    }
                } else {
                    final JsonParser jsonParser = node.get(prop.field().getName()).traverse(mapper);
                    final Object value;
                    if (AbstractEntity.KEY.equals(prop.field().getName())) {
                        value = jsonParser.readValueAs(AnnotationReflector.getKeyType(type));
                    } else {
                        value = mapper.readValue(jsonParser, constructType(mapper.getTypeFactory(), prop.field()));
                    }
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

                    if (!DynamicEntityClassLoader.isEnhanced(type)) {
                        entity.getProperty(prop.field().getName()).setOriginalValue(value);
                    }
                }
                final JsonNode metaPropNode = node.get("@" + prop.field().getName());
                if (metaPropNode.isNull()) {
                    throw new IllegalStateException("EntitySerialiser has got null meta property '@" + prop.field().getName() + "' when reading entity of type [" + type.getName() + "].");
                }
                final MetaProperty metaProperty = entity.getProperty(prop.field().getName());
                provideDirty(metaProperty, metaPropNode);
                provideEditable(metaProperty, metaPropNode);
                provideRequired(metaProperty, metaPropNode);
                provideVisible(metaProperty, metaPropNode);
            }
            //      entity.setInitialising(false);

            return entity;
        }
    }

    /**
     * Retrieves 'dirty' value from entity JSON tree.
     *
     * @param metaProperty
     * @param metaPropNode
     * @return
     */
    private void provideDirty(final MetaProperty metaProperty, final JsonNode metaPropNode) {
        final JsonNode dirtyPropNode = metaPropNode.get("_dirty");
        if (dirtyPropNode == null) {
            // do nothing -- there is no node and that means that there is default value
        } else {
            if (dirtyPropNode.isNull()) {
                throw new IllegalStateException("EntitySerialiser has got null 'dirty' inside meta property '@" + metaProperty.getName() + "' when reading entity of type [" + type.getName() + "].");
            }
            if (metaProperty != null) {
                metaProperty.setDirty(dirtyPropNode.asBoolean());
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
        final Class<?> fieldType = PropertyTypeDeterminator.stripIfNeeded(propertyField.getType());
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

    private Long typeNumber() {
        return entityType.get_number();
    }
}
