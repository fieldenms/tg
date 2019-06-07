package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.generateUnionEntityPropertyExpression;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY_AS_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ONE2ONE_ID;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.PRIMITIVE;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC_COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.YesNoType;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor;
import ua.com.fielden.platform.eql.dbschema.TableDdl;
import ua.com.fielden.platform.utils.StreamUtils;

public class DomainMetadata {
    private static final Logger LOGGER = Logger.getLogger(DomainMetadata.class);

    
    private static final TypeResolver typeResolver = new TypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");
    private static final Type H_STRING = typeResolver.basic("string");
    private static final Type H_BOOLEAN = typeResolver.basic("yes_no");
    private static final Type H_BIG_DECIMAL = typeResolver.basic("big_decimal");
    private static final Type H_BIG_INTEGER = typeResolver.basic("big_integer");

    public static final List<String> specialProps = unmodifiableListOf(ID, KEY, VERSION);

    private final PropertyColumn id;
    private final PropertyColumn version;
    private final PropertyColumn key = new PropertyColumn("KEY_");
    //    private final static PropertyMetadata idProperty(final Class<? extends AbstractEntity<?>> entityType) { return new PropertyMetadata.Builder(AbstractEntity.ID, entityType, /*Long.class,*/ false).column(id).hibType(TypeFactory.basic("long")).type(ID).build();}

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata<?>> persistedEntityMetadataMap;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata> modelledEntityMetadataMap;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, PureEntityMetadata> pureEntityMetadataMap;

    private final List<Class<? extends AbstractEntity<?>>> entityTypes;
    
    private Injector hibTypesInjector;

    public DomainMetadata(//
            final Map<Class, Class> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;

        this.hibTypesDefaults = new ConcurrentHashMap<>(entityTypes.size());
        this.persistedEntityMetadataMap = new ConcurrentHashMap<>(entityTypes.size());
        this.modelledEntityMetadataMap = new ConcurrentHashMap<>(entityTypes.size());
        this.pureEntityMetadataMap = new ConcurrentHashMap<>(entityTypes.size());
        
        this.entityTypes = new ArrayList<>(entityTypes);
        
        // initialise meta-data for basic entity properties, which is RDBMS dependent
        if (dbVersion != DbVersion.ORACLE) {
            id = new PropertyColumn("_ID");
            version = new PropertyColumn("_VERSION");
        } else {
            id = new PropertyColumn("TG_ID");
            version = new PropertyColumn("TG_VERSION");
        }

        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class, Class> entry : hibTypesDefaults.entrySet()) {
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

        // the following operations are a bit heave and benefit from parallel processing
        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
                switch (parentInfo.category) {
                case PERSISTED:
                    persistedEntityMetadataMap.put(entityType, generatePersistedEntityMetadata(parentInfo));
                    break;
                case QUERY_BASED:
                    modelledEntityMetadataMap.put(entityType, generateModelledEntityMetadata(parentInfo));
                    break;
                case UNION:
                    modelledEntityMetadataMap.put(entityType, generateUnionedEntityMetadata(parentInfo));
                    break;
                default:
                    pureEntityMetadataMap.put(entityType, generatePureEntityMetadata(parentInfo));
                    //System.out.println("PURE ENTITY: " + entityType);
                    //throw new IllegalStateException("Not yet supported category: " + baseInfoForDomainMetadata.getCategory(entityType) + " of " + entityType);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
            }
        });
    }
    
    
    /**
     * Generates DDL statements for creating tables, primary keys, indices and foreign keys for all persistent entity types, which includes domain entities and auxiliary platform entities.
     * 
     * @param dialect
     * @return
     */
    public List<String> generateDatabaseDdl(final Dialect dialect) {
        
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibTypesInjector, this.hibTypesDefaults);
        
        final List<Class<? extends AbstractEntity<?>>> persystentTypes = entityTypes.stream().filter(et -> isPersistedEntityType(et)).collect(Collectors.toList());
        
        final List<String> ddlTables = new LinkedList<>();
        final List<String> ddlFKs = new LinkedList<>();
        
        for (final Class<? extends AbstractEntity<?>> entityType : persystentTypes) {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect, ""));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlTables.addAll(tableDefinition.createIndicesSchema(dialect));
            ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
        }
        final List<String> ddl = new LinkedList<>();
        ddl.addAll(ddlTables);
        ddl.addAll(ddlFKs);
        return ddl;
    }

    public List<String> generateDatabaseDdl(final Dialect dialect, final Class<? extends AbstractEntity<?>> type, final Class<? extends AbstractEntity<?>>... types) {
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibTypesInjector, this.hibTypesDefaults);
        
        final List<Class<? extends AbstractEntity<?>>> persystentTypes = StreamUtils.of(type, types).filter(et -> isPersistedEntityType(et)).collect(Collectors.toList());
        
        final List<String> ddlTables = new LinkedList<>();
        final List<String> ddlFKs = new LinkedList<>();
        
        for (final Class<? extends AbstractEntity<?>> entityType : persystentTypes) {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect, "\n"));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlTables.addAll(tableDefinition.createIndicesSchema(dialect));
            ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
        }
        final List<String> ddl = new LinkedList<>();
        ddl.addAll(ddlTables);
        ddl.addAll(ddlFKs);
        return ddl;
    }

    public <ET extends AbstractEntity<?>> PersistedEntityMetadata<ET> generatePersistedEntityMetadata(final EntityTypeInfo <ET> parentInfo)
            throws Exception {
        return new PersistedEntityMetadata(parentInfo.tableName, parentInfo.entityType, generatePropertyMetadatasForEntity(parentInfo));
    }

    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateModelledEntityMetadata(final EntityTypeInfo <ET> parentInfo)
            throws Exception {
        return new ModelledEntityMetadata(parentInfo.entityModels, parentInfo.entityType, generatePropertyMetadatasForEntity(parentInfo));
    }

    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateUnionedEntityMetadata(final EntityTypeInfo <ET> parentInfo)
            throws Exception {
        return new ModelledEntityMetadata(parentInfo.unionEntityModels, parentInfo.entityType, generatePropertyMetadatasForEntity(parentInfo));
    }

    public <ET extends AbstractEntity<?>> PureEntityMetadata<ET> generatePureEntityMetadata(final EntityTypeInfo <ET> parentInfo) {
        return new PureEntityMetadata<>(parentInfo.tableName, parentInfo.entityType);
    }

    public Object getBooleanValue(final boolean value) {
        final Object booleanHibClass = hibTypesDefaults.get(boolean.class);
        if (booleanHibClass instanceof YesNoType) {
            return value ? "Y" : "N";
        }
        if (booleanHibClass instanceof TrueFalseType) {
            return value ? "T" : "F";
        }
        if (booleanHibClass instanceof BooleanType) {
            return value ? 1 : 0;
        }

        throw new IllegalStateException("No appropriate converting hib type found for java boolean type");
    }

    private PropertyMetadata generateIdPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        final PropertyMetadata idProperty = new PropertyMetadata.Builder(ID, Long.class, false, parentInfo).column(id).hibType(H_LONG).category(PRIMITIVE).build();
        final PropertyMetadata idPropertyInOne2One = new PropertyMetadata.Builder(ID, Long.class, false, parentInfo).column(id).hibType(H_LONG).category(ONE2ONE_ID).build();
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
                return new PropertyMetadata.Builder(ID, Long.class, false, parentInfo).hibType(H_LONG).expression(expr().prop(KEY).model()).category(EXPRESSION).build();
            } else {
                return null;
            }
        case UNION:
            return new PropertyMetadata.Builder(ID, Long.class, false, parentInfo).hibType(H_LONG).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) parentInfo.entityType, ID)).category(EXPRESSION).build();
        default:
            return null;
        }
    }

    private PropertyMetadata generateVersionPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
        return PERSISTED == parentInfo.category ? new PropertyMetadata.Builder(VERSION, Long.class, false, parentInfo).column(version).hibType(H_LONG).category(PRIMITIVE).build() : null;
    }
    
    private PropertyMetadata generateKeyPropertyMetadata(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final Class<? extends Comparable> keyType = getKeyType(parentInfo.entityType);
        if (isOneToOne(parentInfo.entityType)) {
            switch (parentInfo.category) {
            case PERSISTED:
                return new PropertyMetadata.Builder(KEY, keyType, false, parentInfo).column(id).hibType(H_LONG).category(ENTITY_AS_KEY).build();
            case QUERY_BASED:
                return new PropertyMetadata.Builder(KEY, keyType, false, parentInfo).hibType(H_LONG).category(SYNTHETIC).build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return getVirtualPropInfoForDynamicEntityKey((EntityTypeInfo <? extends AbstractEntity<DynamicEntityKey>>) parentInfo);
        } else {
            switch (parentInfo.category) {
            case PERSISTED:
                return new PropertyMetadata.Builder(KEY, keyType, false, parentInfo).column(key).hibType(typeResolver.basic(keyType.getName())).category(PRIMITIVE).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                    return new PropertyMetadata.Builder(KEY, keyType, false, parentInfo).column(key).hibType(typeResolver.basic(keyType.getName())).category(PRIMITIVE).build();
                }
                return null; //FIXME
            case UNION:
                return new PropertyMetadata.Builder(KEY, String.class, false, parentInfo).hibType(H_STRING).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) parentInfo.entityType, KEY)).category(EXPRESSION).build();
            default:
                return null;
            }
        }
    }

    /**
     * Generates persistence info for common properties of provided entity type.
     *
     * @param entityType
     * @return
     * @throws Exception
     */
    private SortedMap<String, PropertyMetadata> generatePropertyMetadatasForEntity(final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo)
            throws Exception {
        final SortedMap<String, PropertyMetadata> result = new TreeMap<>();

        safeMapAdd(result, generateIdPropertyMetadata(parentInfo));
        safeMapAdd(result, generateVersionPropertyMetadata(parentInfo));
        safeMapAdd(result, generateKeyPropertyMetadata(parentInfo));

        for (final Field field : getRealProperties(parentInfo.entityType)) {
            if (!result.containsKey(field.getName())) {
                if (Collection.class.isAssignableFrom(field.getType()) && hasLinkProperty(parentInfo.entityType, field.getName())) {
                    safeMapAdd(result, getCollectionalPropInfo(field, parentInfo));
                } else if (isAnnotationPresent(field, Calculated.class)) {
                    safeMapAdd(result, getCalculatedPropInfo(field, parentInfo));
                } else if (isAnnotationPresent(field, MapTo.class)) {
                    safeMapAdd(result, getCommonPropInfo(field, parentInfo));
                } else if (isOne2One_association(parentInfo.entityType, field.getName())) {
                    safeMapAdd(result, getOneToOnePropInfo(field, parentInfo));
                } else if (!isAnnotationPresent(field, CritOnly.class)) {
                    safeMapAdd(result, getSyntheticPropInfo(field, parentInfo));
                } else {
                    //System.out.println(" --------------------------------------------------------- " + entityType.getSimpleName() + ": " + field.getName());
                }
            }
        }

        return result;
    }

    private void safeMapAdd(final Map<String, PropertyMetadata> map, final PropertyMetadata addedItem) {
        if (addedItem != null) {
            map.put(addedItem.getName(), addedItem);

            for (final PropertyMetadata propMetadata : addedItem.getCompositeTypeSubprops()) {
                map.put(propMetadata.getName(), propMetadata);
            }

            for (final PropertyMetadata propMetadata : addedItem.getComponentTypeSubprops()) {
                map.put(propMetadata.getName(), propMetadata);
            }
        }
    }

    /**
     * Generates list of column names for mapping of CompositeUserType implementors.
     *
     * @param hibType
     * @param parentColumn
     * @return
     * @throws Exception
     */
    private List<PropertyColumn> getCompositeUserTypeColumns(final ICompositeUserTypeInstantiate hibType, final String parentColumn) throws Exception {
        final String[] propNames = hibType.getPropertyNames();
        final List<PropertyColumn> result = new ArrayList<>();
        for (final String propName : propNames) {
            final MapTo mapTo = getPropertyAnnotation(MapTo.class, hibType.returnedClass(), propName);
            final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, hibType.returnedClass(), propName);
            final String mapToColumn = mapTo.value();
            final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
            final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
            final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
            final String columnName = propNames.length == 1 ? parentColumn
                    : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
            result.add(new PropertyColumn(columnName, length, precision, scale));
        }
        return result;
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
    private Object getHibernateType(final Class javaType, final PersistentType persistentType, final boolean entity) {
        final String hibernateTypeName = persistentType != null ? persistentType.value() : null;
        final Class hibernateUserTypeImplementor = persistentType != null ? persistentType.userType() : Void.class;

        if (entity) {
            return H_LONG;
        }

        if (isNotEmpty(hibernateTypeName)) {
            return typeResolver.basic(hibernateTypeName);
        }

        if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
            return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
        } else {
            final Object defaultHibType = hibTypesDefaults.get(javaType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                return typeResolver.basic(javaType.getName());
            }
        }
    }

    private List<PropertyColumn> getPropColumns(final Field field, final IsProperty isProperty, final MapTo mapTo, final Object hibernateType) throws Exception {
        final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;

        final List<PropertyColumn> result = new ArrayList<>();
        if (hibernateType instanceof ICompositeUserTypeInstantiate) {
            final ICompositeUserTypeInstantiate hibCompositeUSerType = (ICompositeUserTypeInstantiate) hibernateType;
            for (final PropertyColumn column : getCompositeUserTypeColumns(hibCompositeUSerType, columnName)) {
                result.add(column);
            }
        } else {
            result.add(new PropertyColumn(columnName, length, precision, scale));
        }
        return result;
    }

    private PropertyMetadata getCommonPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final String propName = propField.getName();
        final Class<?> javaType = determinePropertyType(parentInfo.entityType, propName); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final boolean isEntity = isPersistedEntityType(javaType);
        final boolean isUnionEntity = isUnionEntityType(javaType);

        final boolean isCompositeKeyMember = getPropertyAnnotation(CompositeKeyMember.class, parentInfo.entityType, propName) != null;
        final boolean isRequired = isAnnotationPresent(propField, Required.class);
        final PersistentType persistentType = getPersistentType(parentInfo.entityType, propName);
        final boolean nullable = !(isRequired || (isCompositeKeyMember && getPropertyAnnotation(Optional.class, parentInfo.entityType, propName) == null));

        final Object hibernateType = getHibernateType(javaType, persistentType, isEntity);

        PropertyCategory propertyCategory;
        if (isEntity) {
            propertyCategory = isCompositeKeyMember ? ENTITY_MEMBER_OF_COMPOSITE_KEY : ENTITY;
        } else if (isUnionEntity) {
            propertyCategory = UNION_ENTITY_HEADER;
        } else if (hibernateType instanceof ICompositeUserTypeInstantiate) {
            propertyCategory = COMPONENT_HEADER;
        } else {
            propertyCategory = isCompositeKeyMember ? PRIMITIVE_MEMBER_OF_COMPOSITE_KEY : PRIMITIVE;
        }
        
        final MapTo mapTo = getPropertyAnnotation(MapTo.class, parentInfo.entityType, propName);
        final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, parentInfo.entityType, propName);
        return new PropertyMetadata.Builder(propName, javaType, nullable, parentInfo).category(propertyCategory).hibType(hibernateType).columns(getPropColumns(propField, isProperty, mapTo, hibernateType)).build();
    }

    private PropertyMetadata getVirtualPropInfoForDynamicEntityKey(final EntityTypeInfo <? extends AbstractEntity<DynamicEntityKey>> parentInfo) throws Exception {
        return new PropertyMetadata.Builder(KEY, String.class, true, parentInfo).expression(generateCompositeKeyEqlExpression(parentInfo.entityType)).hibType(H_STRING).category(VIRTUAL_OVERRIDE).build();
    }

    private PropertyMetadata getCalculatedPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(getAnnotation(propField, Calculated.class).category());
        
        final Class<?> javaType = determinePropertyType(parentInfo.entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(parentInfo.entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, isPersistedEntityType(javaType));

        final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(parentInfo.entityType, propField);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : EXPRESSION;
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, parentInfo).expression(expressionModel).hibType(hibernateType).category(propCat).aggregatedExpression(aggregatedExpression).build();
    }

    private PropertyMetadata getOneToOnePropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final Class<?> javaType = determinePropertyType(parentInfo.entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(parentInfo.entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, true);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, parentInfo).expression(expressionModel).hibType(hibernateType).category(PropertyCategory.EXPRESSION).build();
    }

    private PropertyMetadata getSyntheticPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final Class<?> javaType = determinePropertyType(parentInfo.entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(parentInfo.entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, false);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? SYNTHETIC_COMPONENT_HEADER : SYNTHETIC;
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, parentInfo).hibType(hibernateType).category(propCat).build();
    }

    private PropertyMetadata getCollectionalPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        return new PropertyMetadata.Builder(propField.getName(), determinePropertyType(parentInfo.entityType, propField.getName()), true, parentInfo).category(COLLECTIONAL).build();
    }

    private PersistentType getPersistentType(final Class entityType, final String propName) {
        return getPropertyAnnotation(PersistentType.class, entityType, propName);
    }

    public Map<Class<?>, Object> getHibTypesDefaults() {
        return hibTypesDefaults;
    }

    public Collection<PersistedEntityMetadata<?>> getPersistedEntityMetadatas() {
        return unmodifiableCollection(persistedEntityMetadataMap.values());
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }

    public Map<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata<?>> getPersistedEntityMetadataMap() {
        return persistedEntityMetadataMap;
    }

    public Map<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata> getModelledEntityMetadataMap() {
        return modelledEntityMetadataMap;
    }

    public Map<Class<? extends AbstractEntity<?>>, PureEntityMetadata> getPureEntityMetadataMap() {
        return pureEntityMetadataMap;
    }
    
    public static String getBooleanValue_(final boolean value) {
        return value ? "Y" : "N";
    }
}

