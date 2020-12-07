package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.generateUnionEntityPropertyExpression;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.hibernate.type.BasicType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

import com.google.inject.Injector;

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
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.metadata.EntityCategory;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.LongPropertyMetadata.Builder;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

public class LongMetadata {
    
    private static final TypeResolver typeResolver = new TypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");
    private static final Type H_STRING = typeResolver.basic("string");
    private static final Type H_BOOLEAN = typeResolver.basic("yes_no");

    public static final List<String> specialProps = unmodifiableListOf(ID, KEY, VERSION);

    private final PropColumn id;
    private final PropColumn version;
    private final PropColumn key = new PropColumn("KEY_");

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, Map<String, LongPropertyMetadata>> entityPropsMetadata;
    private final ConcurrentMap<String, Table> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    
    private Injector hibTypesInjector;

    public LongMetadata(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;

        this.hibTypesDefaults = new ConcurrentHashMap<>(entityTypes.size());
        this.entityPropsMetadata = new ConcurrentHashMap<>(entityTypes.size());

        // initialise meta-data for basic entity properties, which is RDBMS dependent
        if (dbVersion != DbVersion.ORACLE) {
            id = new PropColumn("_ID");
            version = new PropColumn("_VERSION");
        } else {
            id = new PropColumn("TG_ID");
            version = new PropColumn("TG_VERSION");
        }

        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class<?>, Class<?>> entry : hibTypesDefaults.entrySet()) {
                try {
                    this.hibTypesDefaults.put(entry.getKey(), entry.getValue().newInstance());
                } catch (final Exception e) {
                    throw new IllegalStateException("Couldn't generate instantiate hibernate type [" + entry.getValue() + "] due to: " + e);
                }
            }
        }
        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);

        this.hibTypesInjector = hibTypesInjector;

        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
                if (parentInfo.category == PERSISTED || parentInfo.category == QUERY_BASED || parentInfo.category == UNION) {
                    entityPropsMetadata.put(entityType, generatePropertyMetadatasForEntity(parentInfo));
                }
            } catch (final Exception e) {
                e.printStackTrace();
                throw new EqlException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
            }
        });

        tables.putAll(generateTables(entityTypes));

        domainInfo = entityPropsMetadata.keySet().stream().filter(t -> isPersistedEntityType(t) || isSyntheticEntityType(t) || isSyntheticBasedOnPersistentEntityType(t)
                || isUnionEntityType(t)).collect(Collectors.toConcurrentMap(k -> k, k -> new EntityInfo<>(k, determineCategory(k))));
        domainInfo.values().stream().forEach(ei -> addProps(ei, domainInfo, entityPropsMetadata.get(ei.javaType()).values()));
    }
    
    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo, final Collection<LongPropertyMetadata> entityPropsMetadatas) {
        for (final LongPropertyMetadata el : entityPropsMetadatas) {
            final String name = el.name;
            final boolean required = !el.nullable;
            final Class<?> javaType = el.javaType;
            final Object hibType = el.hibType;
            final ExpressionModel expr = el.expressionModel;

            if (isUnionEntityType(javaType)) {
                final EntityInfo<? extends AbstractUnionEntity> ef = new EntityInfo<>((Class<? extends AbstractUnionEntity>) javaType, UNION);
                for (final LongPropertyMetadata sub : el.subitems()) {
                    if (sub.expressionModel == null) {
                        ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, null));
                    } else {
                        final ExpressionModel subExpr = sub.expressionModel;
                        if (EntityUtils.isEntityType(sub.javaType)) {
                            ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, subExpr));
                        } else {
                            ef.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr));
                        }

                    }
                }
                entityInfo.addProp(new UnionTypePropInfo(name, ef, hibType, required));
            } else if (isPersistedEntityType(javaType)) {
                entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(javaType), hibType, required, expr));
                //                } else if (ID.equals(name)){
                //                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(entityInfo.javaType()), hibType, required, expr));
            } else {
                if (el.subitems().isEmpty()) {
                    entityInfo.addProp(new PrimTypePropInfo(name, hibType, javaType, expr));
                } else {
                    final ComponentTypePropInfo propTpi = new ComponentTypePropInfo(name, javaType, hibType);
                    for (final LongPropertyMetadata sub : el.subitems()) {
                        final ExpressionModel subExpr = sub.expressionModel;
                        propTpi.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr));
                    }
                    entityInfo.addProp(propTpi);
                }
            }
        }
    }
    
    private static <ET extends AbstractEntity<?>> EntityCategory determineCategory(final Class<ET> entityType) {
        final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
        return parentInfo.category;
    }
    
    /**
     * Determines hibernate type instance for entity property based on provided property's meta information.
     *
     * @param entityType
     * @param field
     * @return
     * @throws Exception
     * @throws
     */
    private Object getHibernateType(final Field propField) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();
        
        if (isPersistedEntityType(propType) || isUnionEntityType(propType) || (isEntityType(propType) && isSyntheticEntityType((Class<? extends AbstractEntity<?>>)propType))) {
            return H_LONG;
        }

        final PersistentType persistentType = getAnnotation(propField, PersistentType.class);
        
        if (persistentType == null) {
            final Object defaultHibType = hibTypesDefaults.get(propType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                final BasicType result = typeResolver.basic(propType.getName());
                if (result == null) {
                    throw new EqlException(propName + " of type " + propType.getName() + " has no hibType (1)");
                }
                return result;
            }
        } else {
            final String hibernateTypeName = persistentType.value();
            final Class<?> hibernateUserTypeImplementor = persistentType.userType();
            if (isNotEmpty(hibernateTypeName)) {
                final BasicType result = typeResolver.basic(hibernateTypeName);
                if (result == null) {
                    throw new EqlException(propName + " of type " + propType.getName() + " has no hibType (2)");
                }
                return result;
            } else if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
            } else {
                throw new EqlException("Persistent annotation doen't provide intended information.");
            }
        }
    }
    
    private T3<Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate> getHibernateConverter(final Object instance) {
        if (instance instanceof ICompositeUserTypeInstantiate) {
            return t3(null, null, (ICompositeUserTypeInstantiate) instance);
        } else if (instance instanceof IUserTypeInstantiate) {
            return t3(null, (IUserTypeInstantiate) instance, null);
        } else if (instance instanceof Type) {
            return t3((Type) instance, null, null);
        } else {
            throw new EqlException("Can't determine propert hibernate converter"); 
        }
    }
    
    private LongPropertyMetadata generateIdPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final LongPropertyMetadata idProperty = new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).column(id).build();
        final LongPropertyMetadata idPropertyInOne2One = new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).column(id).build();
        switch (parentInfo.category) {
        case PERSISTED:
            return isOneToOne(parentInfo.entityType) ? idPropertyInOne2One : idProperty/*(entityType)*/;
        case QUERY_BASED:
            if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                if (isEntityType(getKeyType(parentInfo.entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persystent type with an entity-typed key. This is not supported.", parentInfo.entityType.getName()));
                }
                return idProperty;
            } else if (isEntityType(getKeyType(parentInfo.entityType))) {
                return new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).expression(expr().prop(KEY).model()).build();
            } else {
                return new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).build(); //return null;
            }
        case UNION:
            return new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) parentInfo.entityType, ID)).build();
        default:
            return null;
        }
    }
    
    private LongPropertyMetadata generateVersionPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        return PERSISTED == parentInfo.category ? new LongPropertyMetadata.Builder(VERSION, Long.class, H_LONG, false).column(version).build() : null;
    }
    
    private LongPropertyMetadata generateKeyPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final Class<? extends Comparable<?>> keyType = getKeyType(parentInfo.entityType);
        if (isOneToOne(parentInfo.entityType)) {
            switch (parentInfo.category) {
            case PERSISTED:
                return new LongPropertyMetadata.Builder(KEY, keyType, H_LONG, false).column(id).build();
            case QUERY_BASED:
                return new LongPropertyMetadata.Builder(KEY, keyType, H_LONG, false).build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return new LongPropertyMetadata.Builder(KEY, String.class, H_STRING, true).expression(generateCompositeKeyEqlExpression((Class<? extends AbstractEntity<DynamicEntityKey>>) parentInfo.entityType)).build();
        } else {
            final Object keyHibType = typeResolver.basic(keyType.getName());
            switch (parentInfo.category) {
            case PERSISTED:
                return new LongPropertyMetadata.Builder(KEY, keyType, keyHibType, false).column(key).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                    return new LongPropertyMetadata.Builder(KEY, keyType, keyHibType, false).column(key).build();
                }
                return new LongPropertyMetadata.Builder(KEY, keyType, keyHibType, false).column(key).build();
            case UNION:
                return new LongPropertyMetadata.Builder(KEY, String.class, H_STRING, false).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) parentInfo.entityType, KEY)).build();
            default:
                return null;
            }
        }
    }
    
    private LongPropertyMetadata generateDescPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        if (parentInfo.category == UNION) {
            return new LongPropertyMetadata.Builder(DESC, String.class, H_STRING, false).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) parentInfo.entityType, DESC)).build(); 
        } else {
            return null; // will be generated via getCommonPropInfo(..)
        }
    }
    
    /**
     * Generates persistence info for common properties of provided entity type.
     *
     * @param entityType
     * @return
     */
    private Map<String, LongPropertyMetadata> generatePropertyMetadatasForEntity(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final Map<String, LongPropertyMetadata> result = new HashMap<>();
        safeMapAdd(result, generateIdPropertyMetadata(parentInfo));
        safeMapAdd(result, generateVersionPropertyMetadata(parentInfo));
        safeMapAdd(result, generateKeyPropertyMetadata(parentInfo));
        safeMapAdd(result, generateDescPropertyMetadata(parentInfo));

        for (final Field field : getRealProperties(parentInfo.entityType)) {
            if (!result.containsKey(field.getName())) {
                if (Collection.class.isAssignableFrom(field.getType()) && hasLinkProperty(parentInfo.entityType, field.getName())) {
                    //safeMapAdd(result, getCollectionalPropInfo(field, parentInfo));
                } else if ((isAnnotationPresent(field, Calculated.class) || isAnnotationPresent(field, MapTo.class) || (parentInfo.category == QUERY_BASED && !isAnnotationPresent(field, CritOnly.class)))) {
                    safeMapAdd(result, getCommonPropInfo(field, parentInfo.entityType, null));
                } else if (isOne2One_association(parentInfo.entityType, field.getName())) {
                    safeMapAdd(result, getOneToOnePropInfo(field, parentInfo));
                } 
            }
        }

        return result;
    }
    
    private void safeMapAdd(final Map<String, LongPropertyMetadata> map, final LongPropertyMetadata addedItem) {
        if (addedItem != null) {
            map.put(addedItem.name, addedItem);
        }
    }
    
    private String getColumnName(final String propName, final MapTo mapTo, final String parentPrefix) {
        return (parentPrefix != null ? parentPrefix + "_" : "") + (isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_");
    }
   
    private PropColumn generateColumn(final String columnName, final IsProperty isProperty) {
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
        return new PropColumn(columnName, length, precision, scale);
    }
    
    /**
     * Generates list of column names for mapping of CompositeUserType implementors.
     *
     * @param hibType
     * @param parentColumnPrefix
     * @return
     * @throws Exception
     */
    private List<LongPropertyMetadata> getCompositeUserTypeColumns(final ICompositeUserTypeInstantiate hibType, final String parentColumnPrefix, final ExpressionModel expr) {
        final String[] propNames = hibType.getPropertyNames();
        final Object[] propHibTypes = hibType.getPropertyTypes();
        final Class<?> headerPropType = hibType.returnedClass();
        final List<LongPropertyMetadata> result = new ArrayList<>();
        for (int i = 0 ; i != propNames.length ; i++) {
            final String propName = propNames[i];
            final MapTo mapTo = getPropertyAnnotation(MapTo.class, headerPropType, propName);
            final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, headerPropType, propName);
            
            final T3<Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate> hibernateTypes3 = getHibernateConverter(propHibTypes[i]);
            final Object subHibType = hibernateTypes3 == null ? null : (hibernateTypes3._1 != null ? hibernateTypes3._1 : (hibernateTypes3._2 != null ? hibernateTypes3._2 : (hibernateTypes3._3 != null ? hibernateTypes3._3 : null)));
            final Class<?> subJavaType = PropertyTypeDeterminator.determinePropertyType(headerPropType, propName);
            
            final LongPropertyMetadata.Builder subLmdInProgress = new LongPropertyMetadata.Builder(propName, subJavaType, subHibType, false);
            if (/*mapTo*/ parentColumnPrefix != null) {
                final String mapToColumn = mapTo.value();
                final String columnName = propNames.length == 1 ? parentColumnPrefix
                        : (parentColumnPrefix + (parentColumnPrefix.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
                result.add(subLmdInProgress.column(generateColumn(columnName, isProperty)).build());
            } else if (expr != null) {
                result.add(subLmdInProgress.expression(expr).build());
            } else {
                result.add(subLmdInProgress.build());
            }
        }
        return result;
    }

    private LongPropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final String parentPrefix) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();

        final boolean nullable = !PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);

        final Object ht = getHibernateType(propField);

        final T3<Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate> hibernateTypes3 = ht == null ? null : getHibernateConverter(ht);
        final Object hibType = hibernateTypes3 == null ? null
                : (hibernateTypes3._1 != null ? hibernateTypes3._1 : (hibernateTypes3._2 != null ? hibernateTypes3._2 : (hibernateTypes3._3 != null ? hibernateTypes3._3 : null)));

        final MapTo mapTo = getAnnotation(propField, MapTo.class);
        final IsProperty isProperty = getAnnotation(propField, IsProperty.class);
        final Calculated calculated = getAnnotation(propField, Calculated.class);

        final Builder resultInProgress = new LongPropertyMetadata.Builder(propName, propType, hibType, nullable);

        if (mapTo != null && !isSyntheticEntityType(entityType) && calculated == null /* 2 last conditions are to overcome incorrect metadata combinations*/) {
            final String columnName = getColumnName(propName, mapTo, parentPrefix);
            if (isUnionEntityType(propType)) {
                final Class<? extends AbstractUnionEntity> unionPropType = (Class<? extends AbstractUnionEntity>) propType;
                final List<Field> propsFields = unionProperties(unionPropType);
                final List<LongPropertyMetadata> subitems = new ArrayList<>();
                subitems.add(new LongPropertyMetadata.Builder(KEY, String.class, H_STRING, false).expression(generateUnionEntityPropertyContextualExpression(unionPropType, KEY, propName)).build());
                subitems.add(new LongPropertyMetadata.Builder(ID, Long.class, H_LONG, false).expression(generateUnionEntityPropertyContextualExpression(unionPropType, ID, propName)).build());

                final List<String> commonProps = commonProperties(unionPropType);
                final List<String> unionPropsNames = propsFields.stream().map(up -> up.getName()).collect(toList());
                final Class<?> firstUnionEntityPropType = propsFields.get(0).getType(); // e.g. WagonSlot in TgBogieLocation
                for (final String commonProp : commonProps) {
                    if (unionPropsNames.contains(commonProp)) {
                        throw new EntityDefinitionException(format("The name of common prop [%s] conflicts with union prop [%s] in union entity [%s].", commonProp, commonProp, unionPropType.getSimpleName()));
                    }
                    final Class<?> javaType = determinePropertyType(firstUnionEntityPropType, commonProp);
                    final Object subHt = getHibernateType(Finder.findFieldByName(firstUnionEntityPropType, commonProp));
                    final T3<Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate> hibernateTypes3_ = getHibernateConverter(subHt);
                    final Object subHibType = hibernateTypes3_ == null ? null
                            : (hibernateTypes3_._1 != null ? hibernateTypes3_._1
                                    : (hibernateTypes3._2 != null ? hibernateTypes3_._2 : (hibernateTypes3_._3 != null ? hibernateTypes3_._3 : null)));

                    subitems.add(new LongPropertyMetadata.Builder(commonProp, javaType, subHibType, false).expression(generateUnionEntityPropertyContextualExpression(unionPropType, commonProp, propName)).build());
                }

                for (final Field subpropField : propsFields) {
                    subitems.add(getCommonPropInfo(subpropField, (Class<? extends AbstractEntity<?>>) propType, columnName));
                }

                return resultInProgress.subitems(subitems).build();
            } else {
                if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                    return resultInProgress.column(generateColumn(columnName, isProperty)).build();
                } else {
                    return resultInProgress.subitems(getCompositeUserTypeColumns((ICompositeUserTypeInstantiate) hibType, columnName, null)).build();
                }
            }
        } else if (calculated != null) {
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.expression(extractExpressionModelFromCalculatedProperty(entityType, propField)).build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeColumns((ICompositeUserTypeInstantiate) hibType, null, extractExpressionModelFromCalculatedProperty(entityType, propField))).build();
            }
        } else { // synthetic entity
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeColumns((ICompositeUserTypeInstantiate) hibType, null, null)).build();
            }
        }
    }
    
    private LongPropertyMetadata getOneToOnePropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object ht = getHibernateType(propField);
        final T3<Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate> hibernateType = ht == null ? null : getHibernateConverter(ht);
        final Object hibType = hibernateType == null ? null : (hibernateType._1 != null ? hibernateType._1 : (hibernateType._2 != null ? hibernateType._2 : (hibernateType._3 != null ? hibernateType._3 : null)));

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new LongPropertyMetadata.Builder(propName, javaType, hibType, true).
                expression(expressionModel).build();
    }

    private PropertyMetadata getCollectionalPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        return new PropertyMetadata.Builder(propField.getName(), determinePropertyType(parentInfo.entityType, propField.getName()), true, parentInfo).category(COLLECTIONAL).build();
    }
    
    public static ExpressionModel generateUnionEntityPropertyContextualExpression(final Class<? extends AbstractUnionEntity> entityType, final String commonPropName, final String contextPropName) {
        final List<Field> props = unionProperties(entityType);
        final Iterator<Field> iterator = props.iterator();
        final String firstUnionPropName = contextPropName + "." + iterator.next().getName();
        ICaseWhenFunctionWhen<IStandAloneExprOperationAndClose, AbstractEntity<?>> expressionModelInProgress = expr().caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName
                + "." + commonPropName);

        for (; iterator.hasNext();) {
            final String unionPropName = contextPropName + "." + iterator.next().getName();
            expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName + "." + commonPropName);
        }

        return expressionModelInProgress.end().model();
    }
    
    private final Map<String, Table> generateTables(final Collection<Class<? extends AbstractEntity<?>>> entities) {
        final Map<String, Table> result = entities.stream()
                .filter(t -> isPersistedEntityType(t))
                .collect(toMap(k -> k.getName(), k -> generateTable(k)));
        return result;
    }
    
    private final Table generateTable(final Class<? extends AbstractEntity<?>> entityType) {
        final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
        final Map<String, String> columns = new HashMap<>();
        for (final LongPropertyMetadata el : entityPropsMetadata.get(entityType).values()) {

            if (el.column != null) {
                columns.put(el.name, el.column.name);
            } else if (!el.subitems().isEmpty()) {
                for (final LongPropertyMetadata subitem : el.subitems()) {
                    if (subitem.expressionModel == null) {
                        final String colName = subitem.column.name.endsWith("_") && subitem.column.name.substring(0, subitem.column.name.length() - 1).contains("_")
                                ? subitem.column.name.substring(0, subitem.column.name.length() - 1)
                                : subitem.column.name;
                        columns.put(el.name + "." + subitem.name, colName);
                    }
                }
            }

        }

        return new Table(parentInfo.tableName, columns);
    }
    
    public Map<String, Table> getTables() {
        return unmodifiableMap(tables);
    }
    
    public EntityInfo<?> getEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }
        
        final Map<String, LongPropertyMetadata> propsMetadatas = generatePropertyMetadatasForEntity(new EntityTypeInfo<>(type));
        entityPropsMetadata.put(type, propsMetadatas);
        final EntityInfo<?> created = new EntityInfo<>(type, determineCategory(type));
        domainInfo.put(type, created);
        addProps(created, domainInfo, propsMetadatas.values());
        return created;
    }
}