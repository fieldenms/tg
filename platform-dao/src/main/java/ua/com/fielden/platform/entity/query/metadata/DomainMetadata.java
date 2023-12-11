package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
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
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfoPair;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.YesNoType;
import org.hibernate.type.spi.TypeConfiguration;

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
import ua.com.fielden.platform.eql.meta.EntityCategory;
import ua.com.fielden.platform.eql.meta.EntityTypeInfo;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.EntityTypeInfo.EntityTypeInfoPair;
import ua.com.fielden.platform.utils.StreamUtils;

public class DomainMetadata {
    private static final Logger LOGGER = getLogger(DomainMetadata.class);
    public final EqlDomainMetadata eqlDomainMetadata;

    private static final TypeResolver typeResolver = new TypeConfiguration().getTypeResolver();
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
    public final boolean eql2;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata<?>> persistedEntityMetadataMap;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata<?>> modelledEntityMetadataMap;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, PureEntityMetadata<?>> pureEntityMetadataMap;

    public final List<Class<? extends AbstractEntity<?>>> entityTypes;
    public final Map<Class<?>, Class<?>> htd = new HashMap<>(); 
    
    public Injector hibTypesInjector;

    public DomainMetadata(//
            final Map<Class, Class> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this(hibTypesDefaults, hibTypesInjector, entityTypes, dbVersion, false);
    }
    
    public DomainMetadata(//
            final Map<Class, Class> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion,
            final boolean eql2) {
        this.dbVersion = dbVersion;
        this.eql2 = eql2;
        
        if (eql2) {
            System.out.println("*** EQL2 mode is on! ***");
        }

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
        
        final Map<Class<?>, Class<?>> htd = new HashMap<>(); 
        
        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class, Class> el : hibTypesDefaults.entrySet()) {
                htd.put(el.getKey(), el.getValue());
            }

            for (final Entry<Class, Class> entry : hibTypesDefaults.entrySet()) {
                htd.put(entry.getKey(), entry.getValue());
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
        
        this.eqlDomainMetadata = eql2 ? null : new EqlDomainMetadata(htd, hibTypesInjector, entityTypes, dbVersion);

        // the following operations are a bit heave and benefit from parallel processing
        entityTypes.parallelStream().forEach(entityType -> {
            try {
                switch (getEntityTypeInfo(entityType).category) {
                case PERSISTENT:
                    persistedEntityMetadataMap.put(entityType, generatePersistedEntityMetadata(entityType, getEntityTypeInfo(entityType)));
                    break;
                case QUERY_BASED:
                    modelledEntityMetadataMap.put(entityType, generateModelledEntityMetadata(getEntityTypeInfoPair(entityType)));
                    break;
                case UNION:
                    modelledEntityMetadataMap.put(entityType, generateUnionedEntityMetadata(getEntityTypeInfoPair(entityType)));
                    break;
                default:
                    pureEntityMetadataMap.put(entityType, generatePureEntityMetadata(entityType));
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
        
        final List<Class<? extends AbstractEntity<?>>> persistentTypes = entityTypes.stream().filter(et -> isPersistedEntityType(et)).collect(Collectors.toList());
        
        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();
        
        for (final Class<? extends AbstractEntity<?>> entityType : persistentTypes) {
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
        
        final List<Class<? extends AbstractEntity<?>>> persistentTypes = StreamUtils.of(type, types).filter(et -> isPersistedEntityType(et)).collect(Collectors.toList());
        
        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();
        
        for (final Class<? extends AbstractEntity<?>> entityType : persistentTypes) {
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

    public <ET extends AbstractEntity<?>> PersistedEntityMetadata<ET> generatePersistedEntityMetadata(final Class<ET> entityType, final EntityTypeInfo<? super ET> entityTypeInfo) throws Exception {
        return new PersistedEntityMetadata<ET>(entityTypeInfo.tableName, entityType, generatePropertyMetadatasForEntity(entityType, entityTypeInfo.category));
    }

    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateModelledEntityMetadata(final EntityTypeInfoPair<ET> entityTypeInfoPair) throws Exception {
        return new ModelledEntityMetadata<ET>(entityTypeInfoPair, generatePropertyMetadatasForEntity(entityTypeInfoPair.entityType(), entityTypeInfoPair.entityTypeInfo().category));
    }

    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateUnionedEntityMetadata(final EntityTypeInfoPair<ET> entityTypeInfoPair) throws Exception {
        final SortedMap<String, PropertyMetadata> propsMetadata = generatePropertyMetadatasForEntity(entityTypeInfoPair.entityType(), entityTypeInfoPair.entityTypeInfo().category);
        final Class<? extends AbstractUnionEntity> entityType = (Class<? extends AbstractUnionEntity>) entityTypeInfoPair.entityType();
        final Set<String> commonProps = commonProperties(entityType);
        final List<Field> unionProps = unionProperties(entityType);
        final List<String> unionPropsNames = unionProps.stream().map(up -> up.getName()).collect(toList());
        final Class<?> unionEntityPropType = unionProps.get(0).getType();
        for (final String propName : commonProps) {
            if (unionPropsNames.contains(propName)) {
                throw new EntityDefinitionException(format("The name of common prop [%s] conflicts with union prop [%s] in union entity [%s].", propName, propName, entityType.getSimpleName()));
            }
            final Class<?> javaType = determinePropertyType(unionEntityPropType, propName);
            safeMapAdd(propsMetadata, new PropertyMetadata.Builder(propName, javaType, false, entityTypeInfoPair.entityTypeInfo().category).expression(generateUnionEntityPropertyExpression(entityType, propName)).category(EXPRESSION).build());
        }

        return new ModelledEntityMetadata<ET>(entityTypeInfoPair, propsMetadata);
    }

    public <ET extends AbstractEntity<?>> PureEntityMetadata<ET> generatePureEntityMetadata(final Class<ET> entityType) {
        return new PureEntityMetadata<>(entityType);
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

    private PropertyMetadata generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) {
        final PropertyMetadata idProperty = new PropertyMetadata.Builder(ID, Long.class, false, category).column(id).hibType(H_LONG).category(PRIMITIVE).build();
        final PropertyMetadata idPropertyInOne2One = new PropertyMetadata.Builder(ID, Long.class, false, category).column(id).hibType(H_LONG).category(ONE2ONE_ID).build();
        switch (category) {
        case PERSISTENT:
            return isOneToOne(entityType) ? idPropertyInOne2One : idProperty;
        case QUERY_BASED:
            if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                if (isEntityType(getKeyType(entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persystent type with an entity-typed key. This is not supported.", entityType.getName()));
                }
                return idProperty;
            } else if (isEntityType(getKeyType(entityType))) {
                return new PropertyMetadata.Builder(ID, Long.class, false, category).hibType(H_LONG).expression(expr().prop(KEY).model()).category(EXPRESSION).build();
            } else {
                return null;
            }
        case UNION:
            return new PropertyMetadata.Builder(ID, Long.class, false, category).hibType(H_LONG).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, ID)).category(EXPRESSION).build();
        default:
            return null;
        }
    }

    private PropertyMetadata generateVersionPropertyMetadata(final EntityCategory category) {
        return PERSISTENT == category ? new PropertyMetadata.Builder(VERSION, Long.class, false, category).column(version).hibType(H_LONG).category(PRIMITIVE).build() : null;
    }
    
    private PropertyMetadata generateKeyPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final Class<? extends Comparable> keyType = getKeyType(entityType);
        if (isOneToOne(entityType)) {
            switch (category) {
            case PERSISTENT:
                return new PropertyMetadata.Builder(KEY, keyType, false, category).column(id).hibType(H_LONG).category(ENTITY_AS_KEY).build();
            case QUERY_BASED:
                return new PropertyMetadata.Builder(KEY, keyType, false, category).hibType(H_LONG).category(SYNTHETIC).build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return getVirtualPropInfoForDynamicEntityKey((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType, category);
        } else {
            switch (category) {
            case PERSISTENT:
                return new PropertyMetadata.Builder(KEY, keyType, false, category).column(key).hibType(typeResolver.basic(keyType.getName())).category(PRIMITIVE).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                    return new PropertyMetadata.Builder(KEY, keyType, false, category).column(key).hibType(typeResolver.basic(keyType.getName())).category(PRIMITIVE).build();
                }
                return null; //FIXME
            case UNION:
                return new PropertyMetadata.Builder(KEY, String.class, false, category).hibType(H_STRING).expression(generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, KEY)).category(EXPRESSION).build();
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
    private SortedMap<String, PropertyMetadata> generatePropertyMetadatasForEntity(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final SortedMap<String, PropertyMetadata> result = new TreeMap<>();

        safeMapAdd(result, generateIdPropertyMetadata(entityType, category));
        safeMapAdd(result, generateVersionPropertyMetadata(category));
        safeMapAdd(result, generateKeyPropertyMetadata(entityType, category));

        for (final Field field : findRealProperties(entityType)) {
            if (!result.containsKey(field.getName())) {
                if (Collection.class.isAssignableFrom(field.getType()) && hasLinkProperty(entityType, field.getName())) {
                    safeMapAdd(result, getCollectionalPropInfo(field, entityType, category));
                } else if (isAnnotationPresent(field, Calculated.class)) {
                    safeMapAdd(result, getCalculatedPropInfo(field, entityType, category));
                } else if (isAnnotationPresent(field, MapTo.class)) {
                    safeMapAdd(result, getCommonPropInfo(field, entityType, category));
                } else if (isOne2One_association(entityType, field.getName())) {
                    safeMapAdd(result, getOneToOnePropInfo(field, entityType, category));
                } else if (!isAnnotationPresent(field, CritOnly.class)) {
                    safeMapAdd(result, getSyntheticPropInfo(field, entityType, category));
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

    private PropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final String propName = propField.getName();
        final Class<?> javaType = determinePropertyType(entityType, propName); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final boolean isEntity = isPersistedEntityType(javaType);
        final boolean isUnionEntity = isUnionEntityType(javaType);

        final boolean isCompositeKeyMember = getPropertyAnnotation(CompositeKeyMember.class, entityType, propName) != null;
        final boolean isRequired = isAnnotationPresent(propField, Required.class);
        final PersistentType persistentType = getPersistentType(entityType, propName);
        final boolean nullable = !(isRequired || (isCompositeKeyMember && getPropertyAnnotation(Optional.class, entityType, propName) == null));

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
        
        final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propName);
        final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, entityType, propName);
        return new PropertyMetadata.Builder(propName, javaType, nullable, category).category(propertyCategory).hibType(hibernateType).columns(getPropColumns(propField, isProperty, mapTo, hibernateType)).build();
    }

    private PropertyMetadata getVirtualPropInfoForDynamicEntityKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType, final EntityCategory category) throws Exception {
        return new PropertyMetadata.Builder(KEY, String.class, true, category).expression(generateCompositeKeyEqlExpression(entityType)).hibType(H_STRING).category(VIRTUAL_OVERRIDE).build();
    }

    private PropertyMetadata getCalculatedPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(getAnnotation(propField, Calculated.class).category());
        
        final Class<?> javaType = determinePropertyType(entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, isPersistedEntityType(javaType));

        final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(entityType, propField);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : EXPRESSION;
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, category).expression(expressionModel).hibType(hibernateType).category(propCat).aggregatedExpression(aggregatedExpression).build();
    }

    private PropertyMetadata getOneToOnePropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final Class<?> javaType = determinePropertyType(entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, true);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, category).expression(expressionModel).hibType(hibernateType).category(PropertyCategory.EXPRESSION).build();
    }

    private PropertyMetadata getSyntheticPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        final Class<?> javaType = determinePropertyType(entityType, propField.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistentType persistentType = getPersistentType(entityType, propField.getName());
        final Object hibernateType = getHibernateType(javaType, persistentType, false);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? SYNTHETIC_COMPONENT_HEADER : SYNTHETIC;
        return new PropertyMetadata.Builder(propField.getName(), propField.getType(), true, category).hibType(hibernateType).category(propCat).build();
    }

    private PropertyMetadata getCollectionalPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final EntityCategory category) throws Exception {
        return new PropertyMetadata.Builder(propField.getName(), determinePropertyType(entityType, propField.getName()), true, category).category(COLLECTIONAL).build();
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

    public Map<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata<?>> getModelledEntityMetadataMap() {
        return modelledEntityMetadataMap;
    }

    public Map<Class<? extends AbstractEntity<?>>, PureEntityMetadata<?>> getPureEntityMetadataMap() {
        return pureEntityMetadataMap;
    }
}
