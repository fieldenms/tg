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
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfo;
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
    private final EntityTypeInfo entityTypeInfo;

    public EntityJsonDeserialiser(final ObjectMapper mapper, final EntityFactory entityFactory, final Class<T> type, final List<CachedProperty> properties, final EntityTypeInfo entityTypeInfo) {
        super(type);
        this.factory = entityFactory;
        this.mapper = mapper;
        this.properties = properties;
        this.entityTypeInfo = entityTypeInfo;

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
                final JsonNode metaPropNode = node.get("@" + prop.field().getName());
                if (metaPropNode.isNull()) {
                    throw new IllegalStateException("EntitySerialiser has got null meta property '@" + prop.field().getName() + "' when reading entity of type [" + type.getName() + "].");
                }
                final JsonNode dirtyPropNode = metaPropNode.get("dirty");
                if (dirtyPropNode.isNull()) {
                    throw new IllegalStateException("EntitySerialiser has got null 'dirty' inside meta property '@" + prop.field().getName() + "' when reading entity of type [" + type.getName() + "].");
                }
                final boolean dirty = dirtyPropNode.asBoolean();
                final JsonNode editablePropNode = metaPropNode.get(MetaProperty.EDITABLE_PROPERTY_NAME);
                if (editablePropNode.isNull()) {
                    throw new IllegalStateException("EntitySerialiser has got null 'editable' inside meta property '@" + prop.field().getName() + "' when reading entity of type [" + type.getName() + "].");
                }
                final boolean editable = editablePropNode.asBoolean();

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
                if (dirty) {
                    entity.getProperty(prop.field().getName()).setDirty(true);
                }
                if (!editable) {
                    entity.getProperty(prop.field().getName()).setEditable(false);
                }
            }
            //      entity.setInitialising(false);

            return entity;
        }
    }

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
        return entityTypeInfo.getNumber();
    }
}
