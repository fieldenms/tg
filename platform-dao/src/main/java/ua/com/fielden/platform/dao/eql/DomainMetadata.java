package ua.com.fielden.platform.dao.eql;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.PURE;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.UNION;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.ENTITY;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.ENTITY_AS_KEY;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.EXPRESSION_COMMON;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.ID;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.ONE2ONE_ID;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.PRIMITIVE;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.PRIMITIVE_AS_KEY;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.VERSION;
import static ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
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

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory;
import ua.com.fielden.platform.dao.eql.PropertyMetadata.PropertyCategory;
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
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

public class DomainMetadata {
    public final static List<String> specialProps = Arrays.asList(new String[] { AbstractEntity.ID, AbstractEntity.KEY, AbstractEntity.VERSION });
    // TODO The new get generation logic should take into account the fact that not all RDBMS systems permit
    // column names starting with underscore (_).
    // For example Oracle 9i (tested) does not.
    // Thus, it is important to provide a way to configure the names on per application or per RDBMS basis.
    private final static PropertyColumn id = new PropertyColumn("_ID");
    private final static PropertyColumn version = new PropertyColumn("_VERSION");
    private final static PropertyColumn key = new PropertyColumn("KEY_");
    //    private final static PropertyMetadata idProperty(final Class<? extends AbstractEntity<?>> entityType) { return new PropertyMetadata.Builder(AbstractEntity.ID, entityType, /*Long.class,*/ false).column(id).hibType(TypeFactory.basic("long")).type(ID).build();}
    private final static PropertyMetadata idProperty = new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).column(id).hibType(TypeFactory.basic("long")).type(ID).build();
    private final static PropertyMetadata idPropertyInOne2One = new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).column(id).hibType(TypeFactory.basic("long")).type(ONE2ONE_ID).build();
    private final static PropertyMetadata versionProperty = new PropertyMetadata.Builder(AbstractEntity.VERSION, Long.class, false).column(version).hibType(TypeFactory.basic("long")).type(VERSION).build();
    private final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final Map<Class, Object> hibTypesDefaults = new HashMap<Class, Object>();
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap = new HashMap<Class<? extends AbstractEntity<?>>, EntityMetadata>();
    private Injector hibTypesInjector;
    private final DomainMetadataExpressionsGenerator dmeg = new DomainMetadataExpressionsGenerator();

    public DomainMetadata(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity<?>>> entityTypes, final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
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
                entityMetadataMap.put(entityType, generateEntityMetadata(entityType, false));
            } catch (final Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
            }
        }

        //enhanceWithCalcProps(entityMetadataMap.values());
    }

    public void enhanceWithCalcProps(final Collection<EntityMetadata> entityMetadatas) {
        for (final EntityMetadata emd : entityMetadatas) {
            if (emd.isPersisted()) {
                try {
                    final PropertyMetadata pmd = getVirtualPropInfoForReferenceCount(DynamicEntityClassLoader.getOriginalType(emd.getType()));
                    safeMapAdd(emd.getProps(), pmd);
                } catch (final Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public <ET extends AbstractEntity<?>> EntityMetadata<ET> generateEntityMetadata(final Class<ET> entityType) throws Exception {
        final EntityMetadata<ET> result = generateEntityMetadata(entityType, true);
        //enhanceWithCalcProps(new HashSet<EntityMetadata>(){{add(result);}});
        return result;
    }

    private <ET extends AbstractEntity<?>> EntityMetadata<ET> generateEntityMetadata(final Class<ET> entityType, final boolean enhanceWithCalcProps) throws Exception {

        final String tableClause = getTableClause(entityType);
        if (tableClause != null) {
            return new EntityMetadata<ET>(tableClause, entityType, generatePropertyMetadatasForEntity(entityType, PERSISTED));
        }

        final List<EntityResultQueryModel<ET>> entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
        if (entityModels.size() > 0) {
            return new EntityMetadata<ET>(entityModels, entityType, generatePropertyMetadatasForEntity(entityType, QUERY_BASED));
        }

        if (isUnionEntityType(entityType)) {
            return new EntityMetadata<ET>(getUnionEntityModels(entityType), entityType, generatePropertyMetadatasForEntity(entityType, UNION));
        } else {
            //System.out.println(" -------------------+++++++++++----------------   " + entityType.getSimpleName());

            return new EntityMetadata<ET>(entityType, generatePropertyMetadatasForEntity(entityType, PURE));
        }
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

    private boolean isOneToOne(final Class<? extends AbstractEntity<?>> entityType) {
        return isPersistedEntityType(getKeyType(entityType));
    }

    private PropertyMetadata generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) {
        switch (entityCategory) {
        case PERSISTED:
            return isOneToOne(entityType) ? idPropertyInOne2One : idProperty/*(entityType)*/;
        case QUERY_BASED:
            if (isEntityType(getKeyType(entityType))) {
                return new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(TypeFactory.basic("long")).expression(expr().prop("key").model()).type(EXPRESSION).build();
            } else {
                return null;
            }
        case UNION:
            return new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(TypeFactory.basic("long")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "id")).type(EXPRESSION).build();
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
                return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(id).hibType(TypeFactory.basic("long")).type(ENTITY_AS_KEY).build();
            case QUERY_BASED:
                return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).hibType(TypeFactory.basic("long")).type(SYNTHETIC).build();
            default:
                return null;
            }
        } else if (!DynamicEntityKey.class.equals(getKeyType(entityType))) {
            switch (entityCategory) {
            case PERSISTED:
                final PropertyColumn keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? new PropertyColumn(getMapEntityTo(entityType).keyColumn()) : key;
                return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic(getKeyType(entityType).getName())).type(PRIMITIVE_AS_KEY).build();
            case QUERY_BASED:
                return null; //FIXME
            case UNION:
                return new PropertyMetadata.Builder(AbstractEntity.KEY, String.class, false).hibType(TypeFactory.basic("string")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "key")).type(EXPRESSION).build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(getKeyType(entityType))) {
            return getVirtualPropInfoForDynamicEntityKey((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType);
        } else {
            return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), true).hibType(TypeFactory.basic(getKeyType(entityType).getName())).build();
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
                    safeMapAdd(result, getPersistedPropHibInfo(entityType, field));
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
    private Object getHibernateType(final Class javaType, final PersistedType persistedType) {
        if (isPersistedEntityType(javaType)) {
            return TypeFactory.basic("long");
        }

        final String hibernateTypeName = persistedType != null ? persistedType.value() : null;
        final Class hibernateUserTypeImplementor = persistedType != null ? persistedType.userType() : Void.class;

        if (isNotEmpty(hibernateTypeName)) {
            return TypeFactory.basic(hibernateTypeName);
        }

        if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
            return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
        } else {
            final Object defaultHibType = hibTypesDefaults.get(javaType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                return TypeFactory.basic(javaType.getName());
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

    private PropertyMetadata getPersistedPropHibInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
        final CommonInfo commonInfo = determineCommonInfo(entityType, field);

        final boolean isEntity = isPersistedEntityType(commonInfo.javaType);
        final boolean isUnionEntity = isUnionEntityType(commonInfo.javaType);
        final MapTo mapTo = getMapTo(entityType, commonInfo.propName);
        final boolean isCompositeKeyMember = getCompositeKeyMember(entityType, commonInfo.propName) != null;
        final boolean isRequired = AnnotationReflector.isAnnotationPresent(field, Required.class);
        final boolean nullable = !(isRequired || isCompositeKeyMember);

        PropertyCategory propertyCategory;
        if (isEntity) {
            propertyCategory = isCompositeKeyMember ? ENTITY_MEMBER_OF_COMPOSITE_KEY : ENTITY;
        } else if (isUnionEntity) {
            propertyCategory = UNION_ENTITY_HEADER;
        } else if (commonInfo.hibernateType instanceof ICompositeUserTypeInstantiate) {
            propertyCategory = COMPONENT_HEADER;
        } else {
            propertyCategory = isCompositeKeyMember ? PRIMITIVE_MEMBER_OF_COMPOSITE_KEY : PRIMITIVE;
        }

        return new PropertyMetadata.Builder(commonInfo.propName, commonInfo.javaType, nullable).type(propertyCategory).hibType(commonInfo.hibernateType).columns(getPropColumns(field, mapTo, commonInfo.hibernateType)).build();
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
        for (final EntityMetadata<? extends AbstractEntity<?>> entityMetadata : entityMetadataMap.values()) {
            if (entityMetadata.isPersisted()) {
                //		System.out.println("                  inspecting props of entity: " + entityMetadata.getType().getSimpleName());

                for (final PropertyMetadata pmd : entityMetadata.getProps().values()) {
                    if (pmd.isEntityOfPersistedType() && pmd.affectsMapping() && pmd.getJavaType().equals(entityType)) {
                        //			System.out.println("                            [" + pmd.getName() + "]");
                        result.add(new Pair(entityMetadata.getType(), pmd.getName()));
                    }
                }
            }
        }
        return result;
    }

    private PropertyMetadata getCalculatedPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final CommonInfo commonInfo = determineCommonInfo(entityType, calculatedPropfield);
        final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(AnnotationReflector.getAnnotation(calculatedPropfield, Calculated.class).category());
        final ExpressionModel expressionModel = dmeg.extractExpressionModelFromCalculatedProperty(entityType, calculatedPropfield);
        final PropertyCategory propCat = commonInfo.hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : EXPRESSION;
        return new PropertyMetadata.Builder(commonInfo.propName, commonInfo.javaType, true).expression(expressionModel).hibType(commonInfo.hibernateType).type(propCat).aggregatedExpression(aggregatedExpression).build();
    }

    private PropertyMetadata getOneToOnePropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final CommonInfo commonInfo = determineCommonInfo(entityType, calculatedPropfield);
        final ExpressionModel expressionModel = expr().prop("id").model();
        return new PropertyMetadata.Builder(commonInfo.propName, commonInfo.javaType, true).expression(expressionModel).hibType(commonInfo.hibernateType).type(PropertyCategory.EXPRESSION).build();
    }

    private PropertyMetadata getSyntheticPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final CommonInfo commonInfo = determineCommonInfo(entityType, calculatedPropfield);
        final PropertyCategory propCat = commonInfo.hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : SYNTHETIC;
        return new PropertyMetadata.Builder(commonInfo.propName, commonInfo.javaType, true).hibType(commonInfo.hibernateType).type(propCat).build();
    }

    private CommonInfo determineCommonInfo(final Class<? extends AbstractEntity<?>> holdingEntityType, final Field propertyField) {
        final String propName = propertyField.getName();
        final Class javaType = determinePropertyType(holdingEntityType, propName); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
        final PersistedType persistedType = getPersistedType(holdingEntityType, propName);
        final Object hibernateType = getHibernateType(javaType, persistedType);
        return new CommonInfo(propName, javaType, persistedType, hibernateType);
    }

    private static class CommonInfo {
        public CommonInfo(final String propName, final Class javaType, final PersistedType persistedType, final Object hibernateType) {
            super();
            this.propName = propName;
            this.javaType = javaType;
            this.persistedType = persistedType;
            this.hibernateType = hibernateType;
        }

        final String propName;
        final Class javaType;
        final PersistedType persistedType;
        final Object hibernateType;
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

    private <ET extends AbstractEntity<?>> List<EntityResultQueryModel<ET>> getUnionEntityModels(final Class<ET> entityType) {
        final List<EntityResultQueryModel<ET>> result = new ArrayList<EntityResultQueryModel<ET>>();
        final List<Field> unionProps = AbstractUnionEntity.unionProperties((Class<? extends AbstractUnionEntity>) entityType);
        for (final Field currProp : unionProps) {
            result.add(generateModelForUnionEntityProperty(unionProps, currProp).modelAsEntity(entityType));
        }
        return result;
    }

    private <PT extends AbstractEntity<?>> ISubsequentCompletedAndYielded<PT> generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp) {
        final IFromAlias<PT> modelInProgress = select((Class<PT>) currProp.getType());
        //	final ISubsequentCompletedAndYielded<PT> modelInProgress = select((Class<PT>) currProp.getType()).yield().prop("key").as("key");
        ISubsequentCompletedAndYielded<PT> m = null;
        for (final Field field : unionProps) {
            if (m == null) {
                m = field.equals(currProp) ? modelInProgress.yield().prop(AbstractEntity.ID).as(field.getName()) : modelInProgress.yield().val(null).as(field.getName());
            } else {
                m = field.equals(currProp) ? m.yield().prop(AbstractEntity.ID).as(field.getName()) : m.yield().val(null).as(field.getName());
            }
        }

        return m;
    }

    private String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
        if (!EntityUtils.isPersistedEntityType(entityType)) {
            return null;
        }

        final MapEntityTo mapEntityToAnnotation = AnnotationReflector.getAnnotation(entityType, MapEntityTo.class);

        final String providedTableName = mapEntityToAnnotation.value();
        if (!StringUtils.isEmpty(providedTableName)) {
            return providedTableName;
        } else {
            return DynamicEntityClassLoader.getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
        }
    }

    public Map<Class, Object> getHibTypesDefaults() {
        return hibTypesDefaults;
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityMetadata> getEntityMetadataMap() {
        return Collections.unmodifiableMap(entityMetadataMap);
    }

    public Collection<EntityMetadata> getEntityMetadatas() {
        return Collections.unmodifiableCollection(entityMetadataMap.values());
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }
}