package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.generateUnionEntityPropertyContextualExpression;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isRequiredByDefinition;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.type.BasicType;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.YesNoType;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.CompositeUserType;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion.PropColumnInfo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata.Builder;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;
import ua.com.fielden.platform.utils.EntityUtils;

public class EqlEntityMetadataGenerator {

    private static final TypeConfiguration typeConfiguration = new TypeConfiguration();
    public static final TypeResolver typeResolver = new TypeResolver(typeConfiguration, new TypeFactory(typeConfiguration));
    public static final Type H_ENTITY = LongType.INSTANCE;
    public static final Type H_LONG = LongType.INSTANCE;
    public static final Type H_INTEGER = IntegerType.INSTANCE;
    public static final Type H_BIGDECIMAL = BigDecimalType.INSTANCE;
    public static final Type H_STRING = StringType.INSTANCE;
    public static final Type H_DATE = DateType.INSTANCE;
    public static final Type H_DATETIME = DateTimeType.INSTANCE;
    public static final Type H_UTCDATETIME = UtcDateTimeType.INSTANCE;
    public static final Type H_BOOLEAN = YesNoType.INSTANCE;

    public static final String Y = "Y";
    public static final String N = "N";

    public static final List<String> SPECIAL_PROPS = unmodifiableListOf(ID, KEY, VERSION);

    private final PropColumn id;
    private final PropColumn version;
    private static final PropColumn key = new PropColumn("KEY_");

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults = new ConcurrentHashMap<>();
    private final Injector hibTypesInjector;

    public EqlEntityMetadataGenerator(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;

        // initialise meta-data for basic entity properties, which is RDBMS dependent
        id = new PropColumn(dbVersion.idColumnName());
        version = new PropColumn(dbVersion.versionColumnName());

        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class<?>, Class<?>> entry : hibTypesDefaults.entrySet()) {
                try {
                    this.hibTypesDefaults.put(entry.getKey(), entry.getValue().getDeclaredField("INSTANCE").get(null));//.newInstance());
                } catch (final Exception e) {
                    throw new EqlMetadataGenerationException("Couldn't generate instantiate hibernate type [" + entry.getValue() + "] due to: " + e);
                }
            }
        }
        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);
        this.hibTypesInjector = hibTypesInjector;
    }

    public <T extends AbstractEntity<?>> EqlEntityMetadataPair<T> generate(final EntityTypeInfo<? super T> typeInfo, final Class<T> entityType) {
        try {
            final Set<EqlPropertyMetadata> propsMetadata = generatePropertyMetadatasForEntity(typeInfo, entityType);
            return new EqlEntityMetadataPair<>(entityType, new EqlEntityMetadata<T>(entityType, typeInfo, propsMetadata));
        } catch (final Exception ex) {
            throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "].", ex);
        }
    }

    private Set<EqlPropertyMetadata> generatePropertyMetadatasForEntity(final EntityTypeInfo<? extends AbstractEntity<?>> typeInfo, final Class<? extends AbstractEntity<?>> entityType) {
        final Set<EqlPropertyMetadata> result = new TreeSet<>();
        if (UNION == typeInfo.category) {
            result.addAll(generateUnionImplicitCalcSubprops((Class<? extends AbstractUnionEntity>) entityType, null));
            unionProperties((Class<? extends AbstractUnionEntity>) entityType).stream().forEach(field -> result.add(getCommonPropInfo(field, entityType, null)));
        } else {
            generateIdPropertyMetadata(entityType, typeInfo.category).ifPresent(idPmd -> result.add(idPmd));

            result.add(generateKeyPropertyMetadata(entityType, typeInfo.category));

            final List<Field> restOfPropsFields = getRestOfProperties(entityType, typeInfo.category);
            final Set<String> addedProps = new HashSet<>();
            addedProps.add(DESC);
            typeInfo.compositeKeyMembers.stream().forEach(f -> addedProps.add(f._1));
            typeInfo.compositeKeyMembers.stream().forEach(f -> restOfPropsFields.stream().filter(p -> f._1.equals(p.getName())).findAny().ifPresent(km -> result.add(getCommonPropInfo(km, entityType, null))));

            restOfPropsFields.stream().filter(p -> DESC.equals(p.getName())).findAny().ifPresent(desc -> result.add(getCommonPropInfo(desc, entityType, null)));

            if (PERSISTENT == typeInfo.category || QUERY_BASED == typeInfo.category && EntityUtils.isPersistedEntityType(entityType.getSuperclass())) {
                result.add(generateVersionPropertyMetadata());
            }

            for (final Field field : restOfPropsFields.stream().filter(p -> !addedProps.contains(p.getName())).collect(toList())) {
                result.add(isOne2One_association(entityType, field.getName()) ? getOneToOnePropInfo(field)
                        : getCommonPropInfo(field, entityType, null));
            }
        }

        return result;
    }

    // TODO there is a duplicate code within HibernateTypeDeterminer.
    /**
     * Determines hibernate type instance (Type/UserType/CustomUserType) for entity property based on provided property's meta information.
     *
     * @param propField
     * @return
     */
    private Object getHibernateType(final Field propField) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();

        if (isPersistedEntityType(propType) || isUnionEntityType(propType) || isSyntheticEntityType(propType)) {
            return H_ENTITY;
        }

        final PersistentType persistentType = getAnnotation(propField, PersistentType.class);

        if (persistentType == null) {
            final Object defaultHibType = hibTypesDefaults.get(propType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                final BasicType result = typeResolver.basic(propType.getName());
                if (result == null) {
                    throw new EqlMetadataGenerationException(propName + " of type " + propType.getName() + " has no hibType (1)");
                }
                return result;
            }
        } else {
            final String hibernateTypeName = persistentType.value();
            final Class<?> hibernateUserTypeImplementor = persistentType.userType();
            if (isNotEmpty(hibernateTypeName)) {
                final BasicType result = typeResolver.basic(hibernateTypeName);
                if (result == null) {
                    throw new EqlMetadataGenerationException(propName + " of type " + propType.getName() + " has no hibType (2)");
                }
                return result;
            } else if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                try {
                    return hibTypesInjector.getInstance(hibernateUserTypeImplementor).getClass().getDeclaredField("INSTANCE").get(null); // need to have the same instance in use for the unit tests sake
                } catch (final Exception e) {
                    throw new EqlMetadataGenerationException("Couldn't obtain instance of hibernate type [" + hibernateUserTypeImplementor + "] due to: " + e);
                }
            } else {
                throw new EqlMetadataGenerationException("Persistent annotation doesn't provide intended information.");
            }
        }
    }

    private Optional<EqlPropertyMetadata> generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) {
    	final EqlPropertyMetadata idProperty = new EqlPropertyMetadata.Builder(ID, Long.class, H_ENTITY).required().column(id).build();
        final EqlPropertyMetadata idPropertyInOne2One = new EqlPropertyMetadata.Builder(ID, Long.class, H_ENTITY).required().column(id).build();
        switch (category) {
        case PERSISTENT:
            return isOneToOne(entityType) ? of(idPropertyInOne2One) : of(idProperty)/*(entityType)*/;
        case QUERY_BASED:
            if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                if (isEntityType(getKeyType(entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persistent type with an entity-typed key. This is not supported.", entityType.getName()));
                }
                return of(idProperty);
            } else if (isEntityType(getKeyType(entityType))) {
                return of(new EqlPropertyMetadata.Builder(ID, Long.class, H_ENTITY).expression(new CalcPropInfo(expr().prop(KEY).model(), true, false)).build());
            } else {
            	// FIXME reconsider this implementation taking into account its role combined with actual yields information in the process of getting final EntityPropInfo for Synthetic Entity
                return of(idProperty);
            }
        default:
            return empty();
        }
    }

    private EqlPropertyMetadata generateVersionPropertyMetadata() {
        return new EqlPropertyMetadata.Builder(VERSION, Long.class, H_LONG).required().column(version).build();
    }

    private EqlPropertyMetadata generateKeyPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) {
        final Class<? extends Comparable<?>> keyType = getKeyType(entityType);
        if (isOneToOne(entityType)) {
            switch (category) {
            case PERSISTENT:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_ENTITY).required().column(id).build();
            case QUERY_BASED:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_ENTITY).required().build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(new CalcPropInfo(generateCompositeKeyEqlExpression((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType), true, false)).required().build();
        } else {
            final Object keyHibType = typeResolver.basic(keyType.getName());
            switch (category) {
            case PERSISTENT:
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                    return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
                }
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().build();
            default:
                return null;
            }
        }
    }

    private ExpressionModel generateUnionCommonDescPropExpressionModel(final List<Field> unionMembers, final String contextPropName) {
        final List<String> unionMembersNames = unionMembers.stream().filter(et -> hasDescProperty((Class<? extends AbstractEntity<?>>) et.getType())).map(et -> et.getName()).collect(toList());
        return generateUnionEntityPropertyContextualExpression(unionMembersNames, DESC, contextPropName);
    }

    private static List<Field> getRestOfProperties(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) {
       return streamRealProperties(entityType)
              .filter(propField -> (isAnnotationPresent(propField, Calculated.class) ||
                      isAnnotationPresent(propField, MapTo.class) ||
                      isAnnotationPresent(propField, CritOnly.class) ||
                      isOne2One_association(entityType, propField.getName()) ||
                      category == QUERY_BASED) &&
                      !SPECIAL_PROPS.contains(propField.getName()) &&
                      !(Collection.class.isAssignableFrom(propField.getType()) && hasLinkProperty(entityType, propField.getName())))
             .collect(toList());
    }

    private static String getColumnName(final String propName, final MapTo mapTo, final String parentPrefix) {
        return (parentPrefix != null ? parentPrefix + "_" : "") + (isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_");
    }

    private static PropColumn generateColumn(final String columnName, final IsProperty isProperty) {
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
        return new PropColumn(removeObsoleteUnderscore(columnName), length, precision, scale);
    }

    private static List<EqlPropertyMetadata> getCompositeUserTypeSubpropsMetadata(final ICompositeUserTypeInstantiate hibType, final String parentColumnPrefix, final CalcPropInfo expr) {
        final String[] propNames = hibType.getPropertyNames();
        final Object[] propHibTypes = hibType.getPropertyTypes();
        final Class<?> headerPropType = hibType.returnedClass();
        final List<EqlPropertyMetadata> result = new ArrayList<>();
        for (int i = 0; i != propNames.length; i++) {
            final String propName = propNames[i];
            final Object subHibType = propHibTypes[i];
            final Class<?> subJavaType = determinePropertyType(headerPropType, propName);

            final EqlPropertyMetadata.Builder subLmdInProgress = new EqlPropertyMetadata.Builder(propName, subJavaType, subHibType);
            if (parentColumnPrefix != null) { //persisted
                final String mapToColumn = getPropertyAnnotation(MapTo.class, headerPropType, propName).value();
                final String columnName = propNames.length == 1 ? parentColumnPrefix
                        : (parentColumnPrefix + (parentColumnPrefix.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
                final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, headerPropType, propName);
                subLmdInProgress.column(generateColumn(columnName, isProperty));
            } else if (expr != null) { // calculated
                subLmdInProgress.expression(expr);
            }

            result.add(subLmdInProgress.build());
        }
        return result;
    }

    private List<EqlPropertyMetadata> generateUnionImplicitCalcSubprops(final Class<? extends AbstractUnionEntity> unionPropType, final String contextPropName) {
        final List<Field> unionMembers = unionProperties(unionPropType);
        final List<String> unionMembersNames = unionMembers.stream().map(up -> up.getName()).collect(toList());
        final List<EqlPropertyMetadata> subitems = new ArrayList<>();
        subitems.add(new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(new CalcPropInfo(generateUnionEntityPropertyContextualExpression(unionMembersNames, KEY, contextPropName), true, false)).build());
        subitems.add(new EqlPropertyMetadata.Builder(ID, Long.class, H_ENTITY).expression(new CalcPropInfo(generateUnionEntityPropertyContextualExpression(unionMembersNames, ID, contextPropName), true, false)).build());
        subitems.add(new EqlPropertyMetadata.Builder(DESC, String.class, H_STRING).expression(new CalcPropInfo(generateUnionCommonDescPropExpressionModel(unionMembers, contextPropName), true, false)).build());

        final List<String> commonProps = commonProperties(unionPropType).stream().filter(n -> !DESC.equals(n) && !KEY.equals(n)).collect(toList());
        final Class<?> firstUnionEntityPropType = unionMembers.get(0).getType(); // e.g. WagonSlot in TgBogieLocation
        for (final String commonProp : commonProps) {
            if (unionMembersNames.contains(commonProp)) {
                throw new EntityDefinitionException(format("The name of common prop [%s] conflicts with union prop [%s] in union entity [%s].", commonProp, commonProp, unionPropType.getSimpleName()));
            }
            final Class<?> javaType = determinePropertyType(firstUnionEntityPropType, commonProp);
            final Object subHibType = getHibernateType(findFieldByName(firstUnionEntityPropType, commonProp));

            subitems.add(new EqlPropertyMetadata.Builder(commonProp, javaType, subHibType).expression(new CalcPropInfo(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName), true, false)).build());
        }

        return subitems;
    }

    private EqlPropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final String parentPrefix) {
        if (isAnnotationPresent(propField, CritOnly.class)) {
            return new EqlPropertyMetadata.Builder(propField.getName(), propField.getType(), null).critOnly().required(isRequiredByDefinition(propField, entityType)).build();
        }

        final String propName = propField.getName();
        final Class<?> propType = propField.getType();
        final Object hibType = getHibernateType(propField);
        final MapTo mapTo = getAnnotation(propField, MapTo.class);
        final IsProperty isProperty = getAnnotation(propField, IsProperty.class);
        final Calculated calculated = getAnnotation(propField, Calculated.class);
        final boolean aggregatedExpression = calculated == null ? false : CalculatedPropertyCategory.AGGREGATED_EXPRESSION == calculated.category();

        final Builder resultInProgress = new EqlPropertyMetadata.Builder(propName, propType, hibType);

        resultInProgress.required(isRequiredByDefinition(propField, entityType));

        if (mapTo != null && !isSyntheticEntityType(entityType) && calculated == null /* 2 last conditions are to overcome incorrect metadata combinations*/) {
            final String columnName = getColumnName(propName, mapTo, parentPrefix);
            if (isUnionEntityType(propType)) {
                final List<EqlPropertyMetadata> subitems = new ArrayList<>();
                final Class<? extends AbstractUnionEntity> unionPropType = (Class<? extends AbstractUnionEntity>) propType;
                subitems.addAll(generateUnionImplicitCalcSubprops(unionPropType, propName));
                for (final Field subpropField : unionProperties(unionPropType)) {
                    subitems.add(getCommonPropInfo(subpropField, (Class<? extends AbstractEntity<?>>) propType, columnName));
                }

                return resultInProgress.subitems(subitems).build();
            } else {
                if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                    return resultInProgress.column(generateColumn(columnName, isProperty)).build();
                } else {
                    return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, columnName, null)).build();
                }
            }
        } else if (calculated != null) {
            final CalcPropInfo calcPropInfo = new CalcPropInfo(extractExpressionModelFromCalculatedProperty(entityType, propField), false, aggregatedExpression);
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.expression(calcPropInfo).build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, calcPropInfo)).build();
            }
        } else { // synthetic entity
            if (isUnionEntityType(propType)) {
                final List<EqlPropertyMetadata> subitems = new ArrayList<>();
                final Class<? extends AbstractUnionEntity> unionPropType = (Class<? extends AbstractUnionEntity>) propType;
                subitems.addAll(generateUnionImplicitCalcSubprops(unionPropType, propName));
                for (final Field subpropField : unionProperties(unionPropType)) {
                    subitems.add(getCommonPropInfo(subpropField, (Class<? extends AbstractEntity<?>>) propType, null));
                }

                return resultInProgress.subitems(subitems).build();
            } else {
                if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                    return resultInProgress.build();
                } else {
                    return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, null)).build();
                }
            }
        }
    }

    private EqlPropertyMetadata getOneToOnePropInfo(final Field propField) {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object hibType = getHibernateType(propField);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new EqlPropertyMetadata.Builder(propName, javaType, hibType).notRequired().expression(new CalcPropInfo(expressionModel, true, false)).build();
    }

    public static EqlTable generateEqlTable(final String tableName, final Set<EqlPropertyMetadata> propsMetadatas) {
        final Map<String, String> columns = new HashMap<>();
        for (final EqlPropertyMetadata el : propsMetadatas) {

            if (el.column != null) {
                columns.put(el.name, el.column.name);
            } else if (!el.subitems.isEmpty()) {
                for (final EqlPropertyMetadata subitem : el.subitems) {
                    if (subitem.expressionModel == null) {
                        columns.put(el.name + "." + subitem.name, subitem.column.name);
                    }
                }
            }
        }

        return new EqlTable(tableName, columns);
    }

    /**
     * Generates a DB table representation, an entity is mapped to, specific to batch insertion.
     *
     * @param tableName
     * @param propsMetadata
     * @return
     */
    public static TableStructForBatchInsertion generateTableWithPropColumnInfo(final String tableName, final Set<EqlPropertyMetadata> propsMetadata) {
        final List<PropColumnInfo> columns = new ArrayList<>();
        for (final EqlPropertyMetadata el : propsMetadata) {
            if (!el.name.equals(ID) && !el.name.equals(VERSION)) {
                if (el.column != null) {
                    columns.add(new PropColumnInfo(isPersistedEntityType(el.javaType) ? el.name + "." + ID : el.name, el.column.name, el.hibType));
                } else if (!el.subitems.isEmpty()) {
                    if (el.hibType instanceof CompositeUserType) {
                        final List<String> columnNames = el.subitems.stream().filter(s -> s.column != null /* only relevant for persistent props */).map(s -> s.column.name).collect(toList());
                        if (!columnNames.isEmpty()) { // there was at least 1 persistent property
                            columns.add(new PropColumnInfo(el.name, columnNames, el.hibType));
                        }
                    } else {
                        for (final EqlPropertyMetadata subitem : el.subitems) {
                            if (subitem.column != null) {
                                columns.add(new PropColumnInfo((el.name + "." + subitem.name + (isPersistedEntityType(subitem.javaType) ? "." + ID : "")), subitem.column.name, subitem.hibType));
                            }
                        }
                    }
                }
            }
        }

        return new TableStructForBatchInsertion(tableName, columns);
    }

    private static String removeObsoleteUnderscore(final String name) {
        return name.endsWith("_") && name.substring(0, name.length() - 1).contains("_")
                ? name.substring(0, name.length() - 1)
                : name;
    }
}