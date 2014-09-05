package ua.com.fielden.platform.dao;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.dao.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.dao.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.dao.EntityCategory.UNION;
import static ua.com.fielden.platform.dao.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.dao.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY_AS_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.dao.PropertyCategory.EXPRESSION_COMMON;
import static ua.com.fielden.platform.dao.PropertyCategory.ONE2ONE_ID;
import static ua.com.fielden.platform.dao.PropertyCategory.PRIMITIVE;
import static ua.com.fielden.platform.dao.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.dao.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.dao.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Hibernate;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

public class DomainMetadata {
    public final static List<String> specialProps = Arrays.asList(new String[] { ID, KEY, VERSION });

    private final PropertyColumn id;
    private final PropertyColumn version;
    private final PropertyColumn key = new PropertyColumn("KEY_");
    //    private final static PropertyMetadata idProperty(final Class<? extends AbstractEntity<?>> entityType) { return new PropertyMetadata.Builder(AbstractEntity.ID, entityType, /*Long.class,*/ false).column(id).hibType(TypeFactory.basic("long")).type(ID).build();}
    private final PropertyMetadata idProperty;
    private final PropertyMetadata idPropertyInOne2One;
    private final PropertyMetadata versionProperty;
    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final Map<Class<?>, Object> hibTypesDefaults = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata> persistedEntityMetadataMap = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata> modelledEntityMetadataMap = new HashMap<>();

    private Injector hibTypesInjector;
    private final DomainMetadataExpressionsGenerator dmeg = new DomainMetadataExpressionsGenerator();

    private final MapEntityTo userMapTo;
    
    public MapEntityTo getUserMapTo() {
        return userMapTo;
    }

    private final TypeResolver typeResolver = new TypeResolver();

    public DomainMetadata(//
            final Map<Class, Class> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final MapEntityTo userMapTo, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
        this.userMapTo = userMapTo;

        // initialise meta-data for basic entity properties, which is RDBMS dependent
        if (dbVersion != DbVersion.ORACLE) {
            id = new PropertyColumn("_ID");
            version = new PropertyColumn("_VERSION");
        } else {
            id = new PropertyColumn("TG_ID");
            version = new PropertyColumn("TG_VERSION");
        }
        
        idProperty = new PropertyMetadata.Builder(ID, Long.class, false).column(id).hibType(typeResolver.basic("long")).type(PRIMITIVE).build();
        idPropertyInOne2One = new PropertyMetadata.Builder(ID, Long.class, false).column(id).hibType(typeResolver.basic("long")).type(ONE2ONE_ID).build();
        versionProperty = new PropertyMetadata.Builder(VERSION, Long.class, false).column(version).hibType(typeResolver.basic("long")).type(PRIMITIVE).build();

        BaseInfoForDomainMetadata baseInfoForDomainMetadata = new BaseInfoForDomainMetadata(userMapTo);
        
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
        this.hibTypesInjector = hibTypesInjector;
        for (final Class<? extends AbstractEntity<?>> entityType : entityTypes) {
            try {
                switch (baseInfoForDomainMetadata.getCategory(entityType)) {
                case PERSISTED:
                    persistedEntityMetadataMap.put(entityType, generatePersistedEntityMetadata(entityType, baseInfoForDomainMetadata));
                    break;
                case QUERY_BASED:
                    modelledEntityMetadataMap.put(entityType, generateModelledEntityMetadata(entityType, baseInfoForDomainMetadata));
                    break;
                case UNION:
                    modelledEntityMetadataMap.put(entityType, generateUnionedEntityMetadata(entityType, baseInfoForDomainMetadata));
                    break;
                default:
                    System.out.println("PURE ENTITY: " + entityType);
                    //throw new IllegalStateException("Not yet supported category: " + baseInfoForDomainMetadata.getCategory(entityType) + " of " + entityType);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
            }
        }

        //enhanceWithCalcProps(entityMetadataMap.values());
    }
    
    public <ET extends AbstractEntity<?>> PersistedEntityMetadata<ET> generatePersistedEntityMetadata(final Class<ET> entityType, BaseInfoForDomainMetadata baseInfoForDomainMetadata) throws Exception {
        return new PersistedEntityMetadata(baseInfoForDomainMetadata.getTableClause(entityType), entityType, generatePropertyMetadatasForEntity(entityType, PERSISTED));
    }
    
    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateModelledEntityMetadata(final Class<ET> entityType, BaseInfoForDomainMetadata baseInfoForDomainMetadata) throws Exception {
        return new ModelledEntityMetadata(baseInfoForDomainMetadata.getEntityModels(entityType), entityType, generatePropertyMetadatasForEntity(entityType, QUERY_BASED));
    }    

    public <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> generateUnionedEntityMetadata(final Class<ET> entityType, BaseInfoForDomainMetadata baseInfoForDomainMetadata) throws Exception {
        return new ModelledEntityMetadata(baseInfoForDomainMetadata.getUnionEntityModels(entityType), entityType, generatePropertyMetadatasForEntity(entityType, UNION));
    }    

    //    public void enhanceWithCalcProps(final Collection<EntityMetadata> entityMetadatas) {
//        for (final EntityMetadata emd : entityMetadatas) {
//            if (emd.isPersisted()) {
//                try {
//                    final PropertyMetadata pmd = getVirtualPropInfoForReferenceCount(DynamicEntityClassLoader.getOriginalType(emd.getType()));
//                    safeMapAdd(emd.getProps(), pmd);
//                } catch (final Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


    
//    public <ET extends AbstractEntity<?>> EntityMetadata<ET> generateEntityMetadata(final Class<ET> entityType) throws Exception {
//
//        final String tableClause = getTableClause(entityType);
//        if (tableClause != null) {
//            return new EntityMetadata<ET>(tableClause, entityType, generatePropertyMetadatasForEntity(entityType, PERSISTED));
//        }
//
//        final List<EntityResultQueryModel<ET>> entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
//        if (entityModels.size() > 0) {
//            return new EntityMetadata<ET>(entityModels, entityType, generatePropertyMetadatasForEntity(entityType, QUERY_BASED));
//        }
//
//        if (isUnionEntityType(entityType)) {
//            return new EntityMetadata<ET>(getUnionEntityModels(entityType), entityType, generatePropertyMetadatasForEntity(entityType, UNION));
//        } else {
//            //System.out.println(" -------------------+++++++++++----------------   " + entityType.getSimpleName());
//
//            return new EntityMetadata<ET>(entityType, generatePropertyMetadatasForEntity(entityType, PURE));
//        }
//    }

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

    private boolean isOneToOne(final Class<? extends AbstractEntity<?>> entityType) {
        return isPersistedEntityType(getKeyType(entityType));
    }

    private PropertyMetadata generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) {
        switch (entityCategory) {
        case PERSISTED:
            return isOneToOne(entityType) ? idPropertyInOne2One : idProperty/*(entityType)*/;
        case QUERY_BASED:
            if (isEntityType(getKeyType(entityType))) {
                return new PropertyMetadata.Builder(ID, Long.class, false).hibType(typeResolver.basic("long")).expression(expr().prop("key").model()).type(EXPRESSION).build();
            } else {
                return null;
            }
        case UNION:
            return new PropertyMetadata.Builder(ID, Long.class, false).hibType(typeResolver.basic("long")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "id")).type(EXPRESSION).build();
        default:
            return null;
        }
    }

    private PropertyMetadata generateVersionPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) {
        return PERSISTED.equals(entityCategory) ? versionProperty : null;
    }

    private PropertyMetadata generateKeyPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) throws Exception {
        if (isOneToOne(entityType)) {
            switch (entityCategory) {
            case PERSISTED:
                return new PropertyMetadata.Builder(KEY, getKeyType(entityType), false).column(id).hibType(typeResolver.basic("long")).type(ENTITY_AS_KEY).build();
            case QUERY_BASED:
                return new PropertyMetadata.Builder(KEY, getKeyType(entityType), false).hibType(typeResolver.basic("long")).type(SYNTHETIC).build();
            default:
                return null;
            }
        } else if (!DynamicEntityKey.class.equals(getKeyType(entityType))) {
            switch (entityCategory) {
            case PERSISTED:
                final PropertyColumn keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? new PropertyColumn(getMapEntityTo(entityType).keyColumn()) : key;
                return new PropertyMetadata.Builder(KEY, getKeyType(entityType), false).column(keyColumnOverride).hibType(typeResolver.basic(getKeyType(entityType).getName())).type(PRIMITIVE).build();
            case QUERY_BASED:
                return null; //FIXME
            case UNION:
                return new PropertyMetadata.Builder(KEY, String.class, false).hibType(typeResolver.basic("string")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "key")).type(EXPRESSION).build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(getKeyType(entityType))) {
            return getVirtualPropInfoForDynamicEntityKey((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType);
        } else {
            return new PropertyMetadata.Builder(KEY, getKeyType(entityType), true).hibType(typeResolver.basic(getKeyType(entityType).getName())).build();
        }
    }

    /**
     * Generates persistence info for common properties of provided entity type.
     *
     * @param entityType
     * @return
     * @throws Exception
     */
    private SortedMap<String, PropertyMetadata> generatePropertyMetadatasForEntity(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory)
            throws Exception {
        final SortedMap<String, PropertyMetadata> result = new TreeMap<String, PropertyMetadata>();

        safeMapAdd(result, generateIdPropertyMetadata(entityType, entityCategory));
        safeMapAdd(result, generateVersionPropertyMetadata(entityType, entityCategory));
        safeMapAdd(result, generateKeyPropertyMetadata(entityType, entityCategory));

        for (final Field field : getRealProperties(entityType)) {
            if (!result.containsKey(field.getName())) {
                if (Collection.class.isAssignableFrom(field.getType()) && Finder.hasLinkProperty(entityType, field.getName())) {
                    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
                } else if (AnnotationReflector.isAnnotationPresent(field, Calculated.class)) {
                    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
                } else if (AnnotationReflector.isAnnotationPresent(field, MapTo.class)) {
                    safeMapAdd(result, getCommonPropHibInfo(entityType, field));
                } else if (Finder.isOne2One_association(entityType, field.getName())) {
                    safeMapAdd(result, getOneToOnePropInfo(entityType, field));
                } else if (!AnnotationReflector.isAnnotationPresent(field, CritOnly.class)) {
                    safeMapAdd(result, getSyntheticPropInfo(entityType, field));
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
        final List<PropertyColumn> result = new ArrayList<PropertyColumn>();
        for (final String propName : propNames) {
            final MapTo mapTo = getMapTo(hibType.returnedClass(), propName);
            final String mapToColumn = mapTo.value();
            final Long length = mapTo.length() > 0 ? new Long(mapTo.length()) : null;
            final Long precision = mapTo.precision() >= 0 ? new Long(mapTo.precision()) : null;
            final Long scale = mapTo.scale() >= 0 ? new Long(mapTo.scale()) : null;
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
    private Object getHibernateType(final Class javaType, final PersistedType persistedType, final boolean entity) {
        final String hibernateTypeName = persistedType != null ? persistedType.value() : null;
        final Class hibernateUserTypeImplementor = persistedType != null ? persistedType.userType() : Void.class;

        if (entity) {
            return typeResolver.basic("long");
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

    private List<PropertyColumn> getPropColumns(final Field field, final MapTo mapTo, final Object hibernateType) throws Exception {
        final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";
        final Long length = mapTo.length() > 0 ? new Long(mapTo.length()) : null;
        final Long precision = mapTo.precision() >= 0 ? new Long(mapTo.precision()) : null;
        final Long scale = mapTo.scale() >= 0 ? new Long(mapTo.scale()) : null;

        final List<PropertyColumn> result = new ArrayList<PropertyColumn>();
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

    private PropertyMetadata getCommonPropHibInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
        final String propName = field.getName();
        final Class javaType = determinePropertyType(entityType, propName); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final boolean isEntity = isPersistedEntityType(javaType);
        final boolean isUnionEntity = isUnionEntityType(javaType);
        final MapTo mapTo = getMapTo(entityType, propName);
        final boolean isCompositeKeyMember = getCompositeKeyMember(entityType, propName) != null;
        final boolean isRequired = AnnotationReflector.isAnnotationPresent(field, Required.class);
        final PersistedType persistedType = getPersistedType(entityType, propName);
        final boolean nullable = !(isRequired || isCompositeKeyMember);

        final Object hibernateType = getHibernateType(javaType, persistedType, isEntity);

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

        return new PropertyMetadata.Builder(propName, javaType, nullable).type(propertyCategory).hibType(hibernateType).columns(getPropColumns(field, mapTo, hibernateType)).build();
    }

    private PropertyMetadata getVirtualPropInfoForDynamicEntityKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType) throws Exception {
        return new PropertyMetadata.Builder("key", String.class, true).expression(dmeg.getVirtualKeyPropForEntityWithCompositeKey(entityType)).hibType(Hibernate.STRING).type(VIRTUAL_OVERRIDE).build();
    }

    private PropertyMetadata getVirtualPropInfoForReferenceCount(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
        return new PropertyMetadata.Builder("referencesCount", Integer.class, true).expression(dmeg.getReferencesCountPropForEntity(getReferences(entityType))).hibType(Hibernate.INTEGER).type(EXPRESSION_COMMON).build();
    }

    public Set<Pair<Class<? extends AbstractEntity<?>>, String>> getReferences(final Class<? extends AbstractEntity<?>> entityType) {
        final Set<Pair<Class<? extends AbstractEntity<?>>, String>> result = new HashSet<>();
        // TODO take into account only PERSISTED props of PERSISTED entities
        //	System.out.println("      getting references for entity: " + entityType.getSimpleName());
        for (final PersistedEntityMetadata<? extends AbstractEntity<?>> entityMetadata : persistedEntityMetadataMap.values()) {
            //		System.out.println("                  inspecting props of entity: " + entityMetadata.getType().getSimpleName());
            for (final PropertyMetadata pmd : entityMetadata.getProps().values()) {
                if (pmd.isEntityOfPersistedType() && pmd.affectsMapping() && pmd.getJavaType().equals(entityType)) {
                    //			System.out.println("                            [" + pmd.getName() + "]");
                    result.add(new Pair(entityMetadata.getType(), pmd.getName()));
                }
            }
        }
        return result;
    }

    private PropertyMetadata getCalculatedPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(AnnotationReflector.getAnnotation(calculatedPropfield, Calculated.class).category());

        final Class javaType = determinePropertyType(entityType, calculatedPropfield.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistedType persistedType = getPersistedType(entityType, calculatedPropfield.getName());
        final Object hibernateType = getHibernateType(javaType, persistedType, false);

        final ExpressionModel expressionModel = dmeg.extractExpressionModelFromCalculatedProperty(entityType, calculatedPropfield);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : EXPRESSION;
        return new PropertyMetadata.Builder(calculatedPropfield.getName(), calculatedPropfield.getType(), true).expression(expressionModel).hibType(hibernateType).type(propCat).aggregatedExpression(aggregatedExpression).build();
    }

    private PropertyMetadata getOneToOnePropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final Class javaType = determinePropertyType(entityType, calculatedPropfield.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistedType persistedType = getPersistedType(entityType, calculatedPropfield.getName());
        final Object hibernateType = getHibernateType(javaType, persistedType, true);

        final ExpressionModel expressionModel = expr().prop("id").model();
        return new PropertyMetadata.Builder(calculatedPropfield.getName(), calculatedPropfield.getType(), true).expression(expressionModel).hibType(hibernateType).type(PropertyCategory.EXPRESSION).build();
    }

    private PropertyMetadata getSyntheticPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final Class javaType = determinePropertyType(entityType, calculatedPropfield.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistedType persistedType = getPersistedType(entityType, calculatedPropfield.getName());
        final Object hibernateType = getHibernateType(javaType, persistedType, false);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : SYNTHETIC;
        return new PropertyMetadata.Builder(calculatedPropfield.getName(), calculatedPropfield.getType(), true).hibType(hibernateType).type(propCat).build();
    }

    private PropertyMetadata getCollectionalPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
        return new PropertyMetadata.Builder(field.getName(), determinePropertyType(entityType, field.getName()), true).type(COLLECTIONAL).build();
    }

    private MapEntityTo getMapEntityTo(final Class entityType) {
        return getAnnotation(entityType, MapEntityTo.class);
    }

    private MapTo getMapTo(final Class entityType, final String propName) {
        return getPropertyAnnotation(MapTo.class, entityType, propName);
    }

    private CompositeKeyMember getCompositeKeyMember(final Class entityType, final String propName) {
        return getPropertyAnnotation(CompositeKeyMember.class, entityType, propName);
    }

    private PersistedType getPersistedType(final Class entityType, final String propName) {
        return getPropertyAnnotation(PersistedType.class, entityType, propName);
    }

    private Calculated getCalculatedPropExpression(final Class entityType, final String propName) {
        return getPropertyAnnotation(Calculated.class, entityType, propName);
    }

    public Map<Class<?>, Object> getHibTypesDefaults() {
        return hibTypesDefaults;
    }

    public Collection<PersistedEntityMetadata> getEntityMetadatas() {
        return Collections.unmodifiableCollection(persistedEntityMetadataMap.values());
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }
    
    public Map<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata> getPersistedEntityMetadataMap() {
        return persistedEntityMetadataMap;
    }

    public Map<Class<? extends AbstractEntity<?>>, ModelledEntityMetadata> getModelledEntityMetadataMap() {
        return modelledEntityMetadataMap;
    }
}