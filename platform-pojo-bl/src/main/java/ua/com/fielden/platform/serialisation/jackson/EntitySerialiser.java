package ua.com.fielden.platform.serialisation.jackson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;
import ua.com.fielden.platform.utils.EntityUtils;

import com.esotericsoftware.kryo.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serialises / deserialises descendants of {@link AbstractEntity}.
 *
 * @author TG Team
 *
 */
public class EntitySerialiser<T extends AbstractEntity<?>> {
    public static final String ENTITY_JACKSON_REFERENCES = "entity-references";
    private final EntityJsonSerialiser<T> serialiser;
    private final EntityJsonDeserialiser<T> deserialiser;
    private final List<CachedProperty> properties;

    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory) {

        // cache all properties annotated with @IsProperty
        properties = createCachedProperties(type);

        serialiser = new EntityJsonSerialiser<T>(type, properties);
        deserialiser = new EntityJsonDeserialiser<T>(mapper, factory, type, properties);

        // register serialiser and deserialiser
        module.addSerializer(type, serialiser);
        module.addDeserializer(type, deserialiser);
    }

    public static <M extends AbstractEntity<?>> String newSerialisationId(final M entity, final References references) {
        return typeId(entity) + "#" + newIdWithinTheType(entity, references);
    }

    public static <M extends AbstractEntity<?>> String typeId(final M entity) {
        // TODO should use type table
        return entity.getType().getName();
    }

    public static <M extends AbstractEntity<?>> String newIdWithinTheType(final M entity, final References references) {
        final Long newId = references.addNewId(entity.getType());
        return newId.toString();
    }

    static private ThreadLocal<JacksonContext> contextThreadLocal = new ThreadLocal<JacksonContext>() {
        @Override
        protected JacksonContext initialValue() {
            return new JacksonContext();
        }
    };

    public List<CachedProperty> createCachedProperties(final Class<T> type) {
        final boolean hasCompositeKey = EntityUtils.isCompositeEntity(type);
        final List<CachedProperty> properties = new ArrayList<CachedProperty>();
        for (final Field propertyField : Finder.findRealProperties(type)) {
            // take into account only persistent properties
            //if (!propertyField.isAnnotationPresent(Calculated.class)) {
            propertyField.setAccessible(true);
            // need to handle property key in a special way -- composite key does not have to be serialised
            if (AbstractEntity.KEY.equals(propertyField.getName())) {
                if (!hasCompositeKey) {
                    final CachedProperty prop = new CachedProperty(propertyField);
                    properties.add(prop);
                    final Class<?> fieldType = AnnotationReflector.getKeyType(type);
                    final int modifiers = fieldType.getModifiers();
                    if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                        prop.setPropertyType(fieldType);
                    }
                }
            } else {
                final CachedProperty prop = new CachedProperty(propertyField);
                properties.add(prop);
                final Class<?> fieldType = PropertyTypeDeterminator.stripIfNeeded(propertyField.getType());
                final int modifiers = fieldType.getModifiers();
                if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                    prop.setPropertyType(fieldType);
                }
            }
            //}
        }
        return Collections.unmodifiableList(properties);
    }

    /**
     * A convenient class to store property related information.
     *
     * @author TG Team
     *
     */
    public final static class CachedProperty {
        private final Field field;
        private Class<?> propertyType;

        CachedProperty(final Field field) {
            this.field = field;
        }

        public Class<?> getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(final Class<?> type) {
            this.propertyType = type;
        }

        public Field field() {
            return field;
        }
    }

    /**
     * Returns the thread local context for serialization and deserialization.
     *
     * @see Context
     */
    static public JacksonContext getContext() {
        return contextThreadLocal.get();
    }
}
