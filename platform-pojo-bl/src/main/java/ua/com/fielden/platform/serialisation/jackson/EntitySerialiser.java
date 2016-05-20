package ua.com.fielden.platform.serialisation.jackson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.*;

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
    private final Class<T> type;
    private final List<CachedProperty> properties;
    private final EntityFactory factory;
    private final EntityType entityTypeInfo;

    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory, final EntityTypeInfoGetter entityTypeInfoGetter, final ISerialisationTypeEncoder serialisationTypeEncoder) {
        this(type, module, mapper, factory, entityTypeInfoGetter, false, serialisationTypeEncoder);
    }

    /**
     * Creates {@link EntitySerialiser} instance based on the specified <code>type</code>.
     * 
     * @param type
     * @param module
     * @param mapper
     * @param factory
     * @param entityTypeInfoGetter
     * @param excludeNulls -- the special switch that indicate whether <code>null</code> properties should be fully disregarded during serialisation into JSON
     * @param serialisationTypeEncoder
     */
    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory, final EntityTypeInfoGetter entityTypeInfoGetter, final boolean excludeNulls, final ISerialisationTypeEncoder serialisationTypeEncoder) {
        this.type = type;
        this.factory = factory;

        // cache all properties annotated with @IsProperty
        properties = createCachedProperties(type);
        this.entityTypeInfo = createEntityTypeInfo(entityTypeInfoGetter);

        final EntityJsonSerialiser<T> serialiser = new EntityJsonSerialiser<T>(type, properties, excludeNulls, serialisationTypeEncoder);
        final EntityJsonDeserialiser<T> deserialiser = new EntityJsonDeserialiser<T>(mapper, factory, type, properties, serialisationTypeEncoder);

        // register serialiser and deserialiser
        module.addSerializer(type, serialiser);
        module.addDeserializer(type, deserialiser);
    }

    private EntityType createEntityTypeInfo(final EntityTypeInfoGetter entityTypeInfoGetter) {
        final EntityType entityTypeInfo = this.factory.newEntity(EntityType.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON)
        entityTypeInfo.beginInitialising();
        entityTypeInfo.setKey(type.getName());

        // let's inform the client of the type's persistence nature
        entityTypeInfo.set_persistent(EntityUtils.isPersistedEntityType(type));
        
        if (EntityUtils.isCompositeEntity(type)) {
            final List<String> compositeKeyNames = new ArrayList<>();
            final List<Field> keyMembers = Finder.getKeyMembers(type);
            for (final Field keyMember : keyMembers) {
                compositeKeyNames.add(keyMember.getName());
            }
            entityTypeInfo.set_compositeKeyNames(compositeKeyNames);

            final String compositeKeySeparator = Reflector.getKeyMemberSeparator((Class<? extends AbstractEntity<DynamicEntityKey>>) type);
            if (!isCompositeKeySeparatorDefault(compositeKeySeparator)) {
                entityTypeInfo.set_compositeKeySeparator(compositeKeySeparator);
            }
        }
        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(type);
        if (!isEntityTitleDefault(type, entityTitleAndDesc.getKey())) {
            entityTypeInfo.set_entityTitle(entityTitleAndDesc.getKey());
        }
        if (!isEntityDescDefault(type, entityTitleAndDesc.getValue())) {
            entityTypeInfo.set_entityDesc(entityTitleAndDesc.getValue());
        }

        if (!properties.isEmpty()) {
            final Map<String, EntityTypeProp> props = new LinkedHashMap<>();
            for (final CachedProperty prop : properties) {
                // non-composite keys should be persisted by identifying their actual type
                final String name = prop.field().getName();
                final EntityTypeProp entityTypeProp = this.factory.newEntity(EntityTypeProp.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON);
                entityTypeProp.beginInitialising();

                //                if (String.class == prop.getPropertyType()) {
                //                    entityTypeProp.set_type("s");
                //                }

                final Boolean secrete = AnnotationReflector.isSecreteProperty(type, name);
                if (!isSecreteDefault(secrete)) {
                    entityTypeProp.set_secrete(secrete);
                }
                final Boolean upperCase = AnnotationReflector.isAnnotationPresentInHierarchy(UpperCase.class, type, name);
                if (!isUpperCaseDefault(upperCase)) {
                    entityTypeProp.set_upperCase(upperCase);
                }
                final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc(name, type);
                entityTypeProp.set_title(titleAndDesc.getKey());
                entityTypeProp.set_desc(titleAndDesc.getValue());
                final Boolean critOnly = AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, type, name);
                if (!isCritOnlyDefault(critOnly)) {
                    entityTypeProp.set_critOnly(critOnly);
                }
                final Boolean resultOnly = AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, type, name);
                if (!isResultOnlyDefault(resultOnly)) {
                    entityTypeProp.set_resultOnly(resultOnly);
                }
                final Boolean ignore = AnnotationReflector.isAnnotationPresentInHierarchy(Ignore.class, type, name);
                if (!isIgnoreDefault(ignore)) {
                    entityTypeProp.set_ignore(ignore);
                }
                final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, type, name);
                if (mapTo != null) {
                    final Long length = mapTo.length();
                    if (!isLengthDefault(length)) {
                        entityTypeProp.set_length(length);
                    }
                    final Long precision = mapTo.precision();
                    if (!isPrecisionDefault(precision)) {
                        entityTypeProp.set_precision(precision);
                    }
                    final Long scale = mapTo.scale();
                    if (!isScaleDefault(scale)) {
                        entityTypeProp.set_scale(scale);
                    }
                }

                entityTypeProp.endInitialising();

                props.put(name, entityTypeProp);
            }
            entityTypeInfo.set_props(props);
        }
        return entityTypeInfoGetter.register(entityTypeInfo);
    }

    public EntityType getEntityTypeInfo() {
        return entityTypeInfo;
    }

    public static <M extends AbstractEntity<?>> String newSerialisationId(final M entity, final References references, final String entityTypeId) {
        return entityTypeId + "#" + newIdWithinTheType(entity, references);
    }

    private static <M extends AbstractEntity<?>> String newIdWithinTheType(final M entity, final References references) {
        final Long newId = references.addNewId(entity.getType());
        return newId.toString();
    }

    static private ThreadLocal<JacksonContext> contextThreadLocal = new ThreadLocal<JacksonContext>() {
        @Override
        protected JacksonContext initialValue() {
            return new JacksonContext();
        }
    };

    public static <T extends AbstractEntity<?>> List<CachedProperty> createCachedProperties(final Class<T> type) {
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
        private boolean entityTyped = false;

        CachedProperty(final Field field) {
            this.field = field;
        }

        public Class<?> getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(final Class<?> type) {
            this.propertyType = type;
            this.entityTyped = EntityUtils.isEntityType(this.propertyType);
        }
        
        public boolean isEntityTyped() {
            return entityTyped;
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
