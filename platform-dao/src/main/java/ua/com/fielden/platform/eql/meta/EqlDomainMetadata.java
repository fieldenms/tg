package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
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
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.generateUnionEntityPropertyContextualExpression;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isRequiredByDefinition;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata.Builder;

public class EqlDomainMetadata {
    
    private static final TypeResolver typeResolver = new TypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");
    private static final Type H_STRING = typeResolver.basic("string");
    private static final Type H_BOOLEAN = typeResolver.basic("yes_no");

    public static final List<String> specialProps = unmodifiableListOf(ID, KEY, VERSION);

    private final PropColumn id;
    private final PropColumn version;
    private static final PropColumn key = new PropColumn("KEY_");

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata;
    
    private Injector hibTypesInjector;

    public EqlDomainMetadata(//
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
                    throw new EqlMetadataGenerationException("Couldn't generate instantiate hibernate type [" + entry.getValue() + "] due to: " + e);
                }
            }
        }
        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);

        this.hibTypesInjector = hibTypesInjector;

        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
                if (parentInfo.category != PURE) {
                    entityPropsMetadata.put(entityType, new EqlEntityMetadata(parentInfo, generatePropertyMetadatasForEntity(parentInfo)));
                }
            } catch (final Exception e) {
                e.printStackTrace();
                throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
            }
        });
   }
    
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
                return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
            } else {
                throw new EqlMetadataGenerationException("Persistent annotation doen't provide intended information.");
            }
        }
    }
    
    private Optional<EqlPropertyMetadata> generateIdPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final EqlPropertyMetadata idProperty = new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).required().column(id).build();
        final EqlPropertyMetadata idPropertyInOne2One = new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).required().column(id).build();
        switch (parentInfo.category) {
        case PERSISTED:
            return isOneToOne(parentInfo.entityType) ? of(idPropertyInOne2One) : of(idProperty)/*(entityType)*/;
        case QUERY_BASED:
            if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                if (isEntityType(getKeyType(parentInfo.entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persistent type with an entity-typed key. This is not supported.", parentInfo.entityType.getName()));
                }
                return of(idProperty);
            } else if (isEntityType(getKeyType(parentInfo.entityType))) {
                return of(new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).expression(expr().prop(KEY).model()).build());
            } else {
                return empty();
            }
        default:
            return empty();
        }
    }
    
    private EqlPropertyMetadata generateVersionPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        return new EqlPropertyMetadata.Builder(VERSION, Long.class, H_LONG).required().column(version).build();
    }
    
    private EqlPropertyMetadata generateKeyPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final Class<? extends Comparable<?>> keyType = getKeyType(parentInfo.entityType);
        if (isOneToOne(parentInfo.entityType)) {
            switch (parentInfo.category) {
            case PERSISTED:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_LONG).required().column(id).build();
            case QUERY_BASED:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_LONG).notRequired().build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(generateCompositeKeyEqlExpression((Class<? extends AbstractEntity<DynamicEntityKey>>) parentInfo.entityType)).build();
        } else {
            final Object keyHibType = typeResolver.basic(keyType.getName());
            switch (parentInfo.category) {
            case PERSISTED:
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                    return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
                }
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).column(key).build();
            default:
                return null;
            }
        }
    }
    
    private ExpressionModel generateUnionCommonDescPropExpressionModel(final List<Field> unionMembers, final String contextPropName) {
        final List<String> unionMembersNames = unionMembers.stream().filter(et -> hasDescProperty((Class<? extends AbstractEntity<?>>) et.getType())).map(et -> et.getName()).collect(Collectors.toList());
        return generateUnionEntityPropertyContextualExpression(unionMembersNames, DESC, contextPropName); 
    }

    private List<EqlPropertyMetadata> generatePropertyMetadatasForEntity(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo) {
        final List<EqlPropertyMetadata> result = new ArrayList<>();
        if (UNION == parentInfo.category) {
            result.addAll(generateUnionImplicitCalcSubprops((Class<? extends AbstractUnionEntity>) parentInfo.entityType, null));
            unionProperties((Class<? extends AbstractUnionEntity>) parentInfo.entityType).stream().forEach(field -> result.add(getCommonPropInfo(field, parentInfo.entityType, null)));
        } else {
            generateIdPropertyMetadata(parentInfo).ifPresent(idPmd -> result.add(idPmd));
            
            result.add(generateKeyPropertyMetadata(parentInfo));
            
            final List<Field> restOfPropsFields = getRestOfProperties(parentInfo);
            final Set<String> addedProps = new HashSet<>();
            addedProps.add(DESC);
            parentInfo.compositeKeyMembers.stream().forEach(f -> addedProps.add(f._1));
            parentInfo.compositeKeyMembers.stream().forEach(f -> restOfPropsFields.stream().filter(p -> f._1.equals(p.getName())).findAny().ifPresent(km -> result.add(getCommonPropInfo(km, parentInfo.entityType, null))));
            
            restOfPropsFields.stream().filter(p -> DESC.equals(p.getName())).findAny().ifPresent(desc -> result.add(getCommonPropInfo(desc, parentInfo.entityType, null)));
            
            if (PERSISTED == parentInfo.category) {
                result.add(generateVersionPropertyMetadata(parentInfo));
            }

            for (final Field field : restOfPropsFields.stream().filter(p -> !addedProps.contains(p.getName())).collect(toList())) {
                result.add(isOne2One_association(parentInfo.entityType, field.getName()) ? getOneToOnePropInfo(field, parentInfo) : getCommonPropInfo(field, parentInfo.entityType, null));
            }
        }

        return result;
    }
    
    public static List<Field> getRestOfProperties(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        return getRealProperties(parentInfo.entityType).
                stream().
                filter(propField -> 
        (
                isAnnotationPresent(propField, Calculated.class) || 
                isAnnotationPresent(propField, MapTo.class) ||
                isOne2One_association(parentInfo.entityType, propField.getName()) || 
                parentInfo.category == QUERY_BASED) &&
        !specialProps.contains(propField.getName()) && 
        !isAnnotationPresent(propField, CritOnly.class) &&
        !(Collection.class.isAssignableFrom(propField.getType()) && hasLinkProperty(parentInfo.entityType, propField.getName()))).
                collect(toList());
    }
    
    private String getColumnName(final String propName, final MapTo mapTo, final String parentPrefix) {
        return (parentPrefix != null ? parentPrefix + "_" : "") + (isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_");
    }
   
    private PropColumn generateColumn(final String columnName, final IsProperty isProperty) {
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
        return new PropColumn(removeObsoleteUnderscore(columnName), length, precision, scale);
    }
    
    private List<EqlPropertyMetadata> getCompositeUserTypeSubpropsMetadata(final ICompositeUserTypeInstantiate hibType, final String parentColumnPrefix, final ExpressionModel expr) {
        final String[] propNames = hibType.getPropertyNames();
        final Object[] propHibTypes = hibType.getPropertyTypes();
        final Class<?> headerPropType = hibType.returnedClass();
        final List<EqlPropertyMetadata> result = new ArrayList<>();
        for (int i = 0 ; i != propNames.length ; i++) {
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
        subitems.add(new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, KEY, contextPropName)).build());
        subitems.add(new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, ID, contextPropName)).build());
        subitems.add(new EqlPropertyMetadata.Builder(DESC, String.class, H_STRING).expression(generateUnionCommonDescPropExpressionModel(unionMembers, contextPropName)).build());

        final List<String> commonProps = commonProperties(unionPropType).stream().filter(n -> !DESC.equals(n)).collect(toList());
        final Class<?> firstUnionEntityPropType = unionMembers.get(0).getType(); // e.g. WagonSlot in TgBogieLocation
        for (final String commonProp : commonProps) {
            if (unionMembersNames.contains(commonProp)) {
                throw new EntityDefinitionException(format("The name of common prop [%s] conflicts with union prop [%s] in union entity [%s].", commonProp, commonProp, unionPropType.getSimpleName()));
            }
            final Class<?> javaType = determinePropertyType(firstUnionEntityPropType, commonProp);
            final Object subHibType = getHibernateType(findFieldByName(firstUnionEntityPropType, commonProp));

            subitems.add(new EqlPropertyMetadata.Builder(commonProp, javaType, subHibType).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName)).build());
        }

        return subitems;
    }

    private EqlPropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final String parentPrefix) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();
        final Object hibType = getHibernateType(propField);
        final MapTo mapTo = getAnnotation(propField, MapTo.class);
        final IsProperty isProperty = getAnnotation(propField, IsProperty.class);
        final Calculated calculated = getAnnotation(propField, Calculated.class);

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
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.expression(extractExpressionModelFromCalculatedProperty(entityType, propField)).build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, extractExpressionModelFromCalculatedProperty(entityType, propField))).build();
            }
        } else { // synthetic entity
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, null)).build();
            }
        }
    }
    
    private EqlPropertyMetadata getOneToOnePropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object hibType = getHibernateType(propField);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new EqlPropertyMetadata.Builder(propName, javaType, hibType).notRequired().expression(expressionModel).build();
    }
    
    public static String removeObsoleteUnderscore(final String name) {
        return name.endsWith("_") && name.substring(0, name.length() - 1).contains("_")
                ? name.substring(0, name.length() - 1)
                :name;
    }
    
    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata() {
        return Collections.unmodifiableMap(entityPropsMetadata);
    }
}