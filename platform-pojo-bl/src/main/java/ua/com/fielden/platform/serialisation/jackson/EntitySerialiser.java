package ua.com.fielden.platform.serialisation.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.shortCollectionKey;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.*;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * Serialises / deserialises descendants of {@link AbstractEntity}.
 *
 * @author TG Team
 *
 */
public class EntitySerialiser<T extends AbstractEntity<?>> {
    public static final String ENTITY_JACKSON_REFERENCES = "entity-references";
    public static final String ID_ONLY_PROXY_PREFIX = "_______id_only_proxy_______";
    private final List<CachedProperty> properties;
    private final EntityType entityTypeInfo;

    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory, final EntityTypeInfoGetter entityTypeInfoGetter, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this(type, module, mapper, factory, entityTypeInfoGetter, false, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache, false);
    }

    /**
     * Creates {@link EntitySerialiser} instance based on the specified <code>type</code>.
     *
     * @param type
     * @param module
     * @param mapper
     * @param factory
     * @param entityTypeInfoGetter
     * @param excludeNulls
     *            -- the special switch that indicate whether <code>null</code> properties should be fully disregarded during serialisation into JSON
     * @param serialisationTypeEncoder
     * @param propertyDescriptorType
     *            -- <code>true</code> to create {@link EntitySerialiser} for {@link PropertyDescriptor} entity type, <code>false</code> otherwise
     */
    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory, final EntityTypeInfoGetter entityTypeInfoGetter, final boolean excludeNulls, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache, final boolean propertyDescriptorType) {
        // cache all properties annotated with @IsProperty
        properties = createCachedProperties(type);
        this.entityTypeInfo = createEntityTypeInfo(type, properties, entityTypeInfoGetter, serialisationTypeEncoder);

        final EntityJsonSerialiser<T> serialiser = new EntityJsonSerialiser<>(type, properties, this.entityTypeInfo, excludeNulls, propertyDescriptorType);
        final EntityJsonDeserialiser<T> deserialiser = new EntityJsonDeserialiser<>(mapper, factory, type, properties, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache, propertyDescriptorType);

        // register serialiser and deserialiser
        module.addSerializer(type, serialiser);
        module.addDeserializer(type, deserialiser);
    }

    private static <T extends AbstractEntity<?>> EntityType createEntityTypeInfo(final Class<T> type, final List<CachedProperty> properties, final EntityTypeInfoGetter entityTypeInfoGetter, final ISerialisationTypeEncoder serialisationTypeEncoder) {
        final EntityType entityTypeInfo = newPlainEntity(EntityType.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON)
        entityTypeInfo.setKey(type.getName());

        // let's inform the client of the type's persistence nature
        if (isPersistentEntityType(type)) {
            entityTypeInfo.set_persistent(true);
        }

        // let's inform the client of the type's persistence nature with audit data
        if (isPersistentWithAuditData(type)) {
            entityTypeInfo.set_persistentWithAudit(true);
        }

        if (isContinuationData(type)) {
            entityTypeInfo.set_continuation(true);
        }

        // let's inform the client of whether value descriptions should be displayed in editors of this type
        final boolean shouldDisplayDescription = AnnotationReflector.isAnnotationPresentForClass(DisplayDescription.class, type);
        if (!isDisplayDescDefault(shouldDisplayDescription)) {
            entityTypeInfo.set_displayDesc(shouldDisplayDescription);
        }

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
            final var titleAndDesc = getTitleAndDesc(KEY, type);
            entityTypeInfo.set_compositeKey_title(titleAndDesc.getKey());
            entityTypeInfo.set_compositeKey_desc(titleAndDesc.getValue());
        }
        if (AbstractUnionEntity.class.isAssignableFrom(type)) {
            entityTypeInfo.set_unionCommonProps(new ArrayList<>(commonProperties((Class<AbstractUnionEntity>) type)));
            entityTypeInfo.set_unionProps(unionProperties((Class<AbstractUnionEntity>) type).stream().map(filed -> filed.getName()).collect(toList()));
        }
        if (AbstractFunctionalEntityToOpenCompoundMaster.class.isAssignableFrom(type)) {
            entityTypeInfo.set_compoundOpenerType(getKeyType(type).getName());
        }
        if (AbstractFunctionalEntityForCompoundMenuItem.class.isAssignableFrom(type)) {
            entityTypeInfo.set_compoundMenuItem(true);
        }
        // Only set to non-'null' value for specific types having base type.
        // 'null' values will not be serialised as a JSON properties in a type table.
        entityTypeInfo.set_baseType(maybeBaseTypeForSyntheticEntityOrSingleKeyMemberEntityType(type).map(Class::getName).orElse(null));
        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(type);
        if (!isEntityTitleDefault(type, entityTitleAndDesc.getKey())) {
            entityTypeInfo.set_entityTitle(entityTitleAndDesc.getKey());
        }
        if (!isEntityDescDefault(type, entityTitleAndDesc.getValue())) {
            entityTypeInfo.set_entityDesc(entityTitleAndDesc.getValue());
        }

        // extend type information with common union properties to facilitate client-side common props meta-information recognition (e.g. property type, @DateOnly, @UpperCase etc.)
        final List<CachedProperty> propertiesWithCommonUnionProps = new ArrayList<>();
        propertiesWithCommonUnionProps.addAll(properties);
        if (isUnionEntityType(type)) {
            propertiesWithCommonUnionProps.addAll(
                commonProperties((Class<? extends AbstractUnionEntity>) type).stream() // composite 'key' property is never included in common props set
                .map(commonProp -> new CachedProperty(getFieldByName(type, commonProp), type))
                .collect(toList())
            );
        }
        if (!propertiesWithCommonUnionProps.isEmpty()) {
            final Map<String, EntityTypeProp> props = new LinkedHashMap<>();
            for (final CachedProperty prop : propertiesWithCommonUnionProps) {
                // non-composite keys should be persisted by identifying their actual type
                final String name = prop.field().getName();
                final EntityTypeProp entityTypeProp = newPlainEntity(EntityTypeProp.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON)

                final Boolean secrete = AnnotationReflector.isSecreteProperty(type, name);
                if (!isSecreteDefault(secrete)) {
                    entityTypeProp.set_secrete(secrete);
                }
                final Boolean upperCase = AnnotationReflector.isAnnotationPresentInHierarchy(UpperCase.class, type, name);
                if (!isUpperCaseDefault(upperCase)) {
                    entityTypeProp.set_upperCase(upperCase);
                }
                final Pair<String, String> titleAndDesc = getTitleAndDesc(name, type);
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
                if (isPropertyAnnotationPresent(DateOnly.class, type, name)) {
                    entityTypeProp.set_date(TRUE);
                }
                if (isPropertyAnnotationPresent(TimeOnly.class, type, name)) {
                    entityTypeProp.set_time(TRUE);
                }
                final IsProperty isPropertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, type, name);
                if (isPropertyAnnotation != null) {
                    final Long length = Long.valueOf(isPropertyAnnotation.length());
                    if (!isLengthDefault(length)) {
                        entityTypeProp.set_length(length);
                    }
                    final Long precision = Long.valueOf(isPropertyAnnotation.precision());
                    if (!isPrecisionDefault(precision)) {
                        entityTypeProp.set_precision(precision);
                    }
                    final Long scale = Long.valueOf(isPropertyAnnotation.scale());
                    if (!isScaleDefault(scale)) {
                        entityTypeProp.set_scale(scale);
                    }
                    final boolean trailingZeros = isPropertyAnnotation.trailingZeros();
                    if (isTrailingZerosDefault(trailingZeros)) {
                        entityTypeProp.set_trailingZeros(trailingZeros);
                    }
                    final String displayAs = isPropertyAnnotation.displayAs();
                    if (!isDisplayAsDefault(displayAs)) {
                        entityTypeProp.set_displayAs(displayAs);
                    }
                }
                final String timeZone = getTimeZone(type, name);
                if (timeZone != null) {
                    entityTypeProp.set_timeZone(timeZone);
                }
                entityTypeProp.set_typeName(typeNameOf(prop));

                if (isShortCollection(type, name)) {
                    entityTypeProp.set_shortCollectionKey(shortCollectionKey(type, name));
                }

                if (isPropertyAnnotationPresent(EntityTypeCarrier.class, type, name)) {
                    entityTypeProp.set_entityTypeCarrier(TRUE);
                }

                props.put(name, entityTypeProp);
            }
            entityTypeInfo.set_props(props);
        }
        return entityTypeInfoGetter.register(entityTypeInfo);
    }

    /**
     * Returns {@link String} representation of the type of the specified <code>prop</code> in case if it is supported by means of {@link #isFieldTypeSupported(Class)}.
     * For entity-typed properties (non-collectional) it returns full class name prepended with ":", for other types it returns simple class name.
     *
     * @param prop
     * @return
     */
    private static String typeNameOf(final CachedProperty prop) {
        return prop.getPropertyType() == null ? null : (prop.isEntityTyped() ? ":" + prop.getPropertyType().getName() : prop.getPropertyType().getSimpleName());
    }

    public EntityType getEntityTypeInfo() {
        return entityTypeInfo;
    }

    public static <M extends AbstractEntity<?>> String newSerialisationId(final M entity, final References references, final String entityTypeName) {
        return entityTypeName + "#" + newIdWithinTheType(entity, references);
    }

    private static <M extends AbstractEntity<?>> String newIdWithinTheType(final M entity, final References references) {
        final Long newId = references.addNewId(entity.getType());
        return newId.toString();
    }

    private static ThreadLocal<JacksonContext> contextThreadLocal = ThreadLocal.withInitial(JacksonContext::new);

    /**
     * Returns <code>true</code> if the specified <code>fieldType</code> is supported for {@link CachedProperty}, otherwise <code>false</code>.
     * <p>
     * Only non-abstract and non-interface types are supported. The only exception is boolean.class which is supported but has 'abstract' modifier.
     *
     * @param fieldType
     * @return
     */
    private static boolean isFieldTypeSupported(final Class<?> fieldType) {
        final int modifiers = fieldType.getModifiers();
        return boolean.class == fieldType || !isAbstract(modifiers) && !isInterface(modifiers);
    }

    /**
     * Creates list of {@link CachedProperty} instances for the specified <code>type</code> based on its property definitions.
     *
     * @param type
     * @return
     */
    public static <T extends AbstractEntity<?>> List<CachedProperty> createCachedProperties(final Class<T> type) {
        return unmodifiableList(
            streamRealProperties(type) // composite 'key' field is not included in real props set
            .map(field -> new CachedProperty(field, type))
            .collect(toList())
        );
    }

    /**
     * A convenient class to store property related information.
     *
     * @author TG Team
     *
     */
    public static final class CachedProperty {
        private final Field field;
        private Class<?> propertyType;
        private boolean entityTyped = false;

        CachedProperty(final Field field, final Class<? extends AbstractEntity<?>> entityType) {
            this.field = field;
            field.setAccessible(true);
            final Class<?> fieldType = KEY.equals(field.getName()) ? getKeyType(entityType) : stripIfNeeded(field.getType());
            if (isFieldTypeSupported(fieldType)) {
                setPropertyType(fieldType);
            }
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
     */
    public static JacksonContext getContext() {
        return contextThreadLocal.get();
    }
}
