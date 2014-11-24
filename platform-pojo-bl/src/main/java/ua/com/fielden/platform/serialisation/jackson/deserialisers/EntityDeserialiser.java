package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class EntityDeserialiser<T extends AbstractEntity<?>> extends JsonDeserializer<T> {

    private final EntityFactory entityFactory;
    private final ObjectMapper mapper;

    public EntityDeserialiser(final ObjectMapper mapper, final EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        //TODO Right now it is a QueryRunner deserialiser but in the future when json object will have _type property it can be deserilised in to any type.
        try {
            final JsonNode node = jp.readValueAsTree();
            final Class<T> entityType = (Class<T>) findClass(node.get("@entityType").asText());
            final T entity = entityFactory.newEntity(entityType);
            //	    entity.setInitialising(true);
            for (final Field propertyField : Finder.findRealProperties(entity.getType())) {
                final JsonNode propNode = node.get(propertyField.getName());
                if (propNode != null) {
                    propertyField.setAccessible(true);
                    if (AbstractEntity.KEY.equals(propertyField.getName())) {
                        final Class<?> fieldType = AnnotationReflector.getKeyType(entityType);
                        propertyField.set(entity, node.get(propertyField.getName()).traverse(mapper).readValueAs(fieldType));
                    } else {
                        propertyField.set(entity, mapper.readValue(node.get(propertyField.getName()).traverse(mapper), constructType(mapper.getTypeFactory(), propertyField)));
                        // node.get(propertyField.getName()).traverse(mapper).readValueAs());
                    }

                }
            }
            //	    entity.setInitialising(false);
            return entity;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while deserialising");
        }
    }

    private ResolvedType constructType(final TypeFactory typeFactory, final Field propertyField) {
        final Class<?> fieldType = PropertyTypeDeterminator.stripIfNeeded(propertyField.getType());

        if (Map.class.isAssignableFrom(fieldType)) {
            final ParameterizedType paramType = (ParameterizedType) propertyField.getGenericType();
            final Class<?> keyClass = PropertyTypeDeterminator.classFrom(paramType.getActualTypeArguments()[0]);
            final Class<?> valueClass = PropertyTypeDeterminator.classFrom(paramType.getActualTypeArguments()[1]);

            return typeFactory.constructMapType((Class<? extends Map>) fieldType, keyClass, valueClass);
        } else {
            // TODO no other collectional types are supported at this stage -- should be added one by one
            return typeFactory.constructType(PropertyTypeDeterminator.stripIfNeeded(propertyField.getType()));
        }
    }

}
