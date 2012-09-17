package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.EntityMetadata.EntityCategory;
import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
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
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Injector;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.dao.EntityMetadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.dao.EntityMetadata.EntityCategory.PURE;
import static ua.com.fielden.platform.dao.EntityMetadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.dao.EntityMetadata.EntityCategory.UNION;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.ENTITY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.ENTITY_KEY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.ID;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.ONE2ONE_ID;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.PRIMITIVE_KEY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.PROP;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.UNION_ENTITY;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.VERSION;
import static ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

public class DomainMetadata {
    public final static List<String> specialProps = Arrays.asList(new String[] { AbstractEntity.ID, AbstractEntity.KEY, AbstractEntity.VERSION });
    private final static PropertyColumn id = new PropertyColumn("_ID");
    private final static PropertyColumn version = new PropertyColumn("_VERSION");
    private final static PropertyColumn key = new PropertyColumn("KEY_");
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
		entityMetadataMap.put(entityType, generateEntityMetadata(entityType));
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalStateException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
	    }
	}
    }

    public <ET extends AbstractEntity<?>> EntityMetadata<ET> generateEntityMetadata(final Class<ET> entityType) throws Exception {

	final String tableClause = getTableClause(entityType);
	if (tableClause != null) {
	    return new EntityMetadata<ET>(tableClause, entityType, generatePropertyMetadatasForEntity(entityType, PERSISTED));
	}

	final List<EntityResultQueryModel<ET>> entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
	if (entityModels.size() > 0) {
	    return new EntityMetadata<ET>(entityModels, entityType, generatePropertyMetadatasForEntity(entityType, QUERY_BASED));
	}

	if (isUnionEntityType(entityType)) {
	    return new EntityMetadata<ET>(getUnionEntityModels(entityType), entityType, generatePropertyMetadatasForEntity(entityType,  UNION));
	} else {
	    return new EntityMetadata<ET>(entityType, generatePropertyMetadatasForEntity(entityType,  PURE));
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
	return AbstractEntity.class.isAssignableFrom(getKeyType(entityType));
    }

    private PropertyMetadata generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) {
	switch (entityCategory) {
	case PERSISTED:
	    return isOneToOne(entityType) ? idPropertyInOne2One : idProperty;
	case QUERY_BASED:
	    return new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(TypeFactory.basic("long")).expression(expr().prop("key").model()).type(EXPRESSION).build();
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
		return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(id).hibType(TypeFactory.basic("long")).type(ENTITY_KEY).build();
	    case QUERY_BASED:
		return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).hibType(TypeFactory.basic("long")).type(SYNTHETIC).build();
	    default:
		return null;
	    }
	} else if (!DynamicEntityKey.class.equals(getKeyType(entityType))) {
	    switch (entityCategory) {
	    case PERSISTED:
		final PropertyColumn keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? new PropertyColumn(getMapEntityTo(entityType).keyColumn()) : key;
		return new PropertyMetadata.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic(getKeyType(entityType).getName())).type(PRIMITIVE_KEY).build();
	    case QUERY_BASED:
		return null; //FIXME
	    case UNION:
		return new PropertyMetadata.Builder(AbstractEntity.KEY, String.class, false).hibType(TypeFactory.basic("string")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "key")).type(EXPRESSION).build();
	    default:
		return null;
	    }
	} else if (DynamicEntityKey.class.equals(getKeyType(entityType))) {
	    return getVirtualPropInfoForDynamicEntityKey(entityType);
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
    private SortedMap<String, PropertyMetadata> generatePropertyMetadatasForEntity(final Class<? extends AbstractEntity<?>> entityType, final EntityCategory entityCategory) throws Exception {
	final SortedMap<String, PropertyMetadata> result = new TreeMap<String, PropertyMetadata>();

	safeMapAdd(result, generateIdPropertyMetadata(entityType, entityCategory));
	safeMapAdd(result, generateVersionPropertyMetadata(entityType, entityCategory));
	safeMapAdd(result, generateKeyPropertyMetadata(entityType, entityCategory));

	for (final Field field : getRealProperties(entityType)) {
	    if (!result.containsKey(field.getName())) {
		if (Collection.class.isAssignableFrom(field.getType()) && Finder.hasLinkProperty(entityType, field.getName())) {
		    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
		} else if (field.isAnnotationPresent(Calculated.class)) {
		    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
		} else if (field.isAnnotationPresent(MapTo.class)) {
		    safeMapAdd(result, getCommonPropHibInfo(entityType, field));
		} else if (Finder.isOne2One_association(entityType, field.getName())) {
		    safeMapAdd(result, getOneToOnePropInfo(entityType, field));
		} else if (!field.isAnnotationPresent(CritOnly.class)) {
		    safeMapAdd(result, getSyntheticPropInfo(entityType, field));
		} else {

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
	    return TypeFactory.basic("long");
	}

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

    private PropertyMetadata getCommonPropHibInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
	final String propName = field.getName();
	final Class javaType = determinePropertyType(entityType, propName); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final boolean isEntity = isPersistedEntityType(javaType);
	final boolean isUnionEntity = isUnionEntityType(javaType);
	final MapTo mapTo = getMapTo(entityType, propName);
	final boolean isCompositeKeyMember = getCompositeKeyMember(entityType, propName) != null;
	final boolean isRequired = field.isAnnotationPresent(Required.class);
	final PersistedType persistedType = getPersistedType(entityType, propName);
	final boolean nullable = !(isRequired || isCompositeKeyMember);

	final Object hibernateType = getHibernateType(javaType, persistedType, isEntity);

	PropertyCategory propertyCategory;
	if (isEntity) {
	    propertyCategory = isCompositeKeyMember ? ENTITY_MEMBER_OF_COMPOSITE_KEY : ENTITY;
	} else if (isUnionEntity) {
	    propertyCategory = UNION_ENTITY;
	} else if (hibernateType instanceof ICompositeUserTypeInstantiate) {
	    propertyCategory = COMPONENT_HEADER;
	} else {
	    propertyCategory = isCompositeKeyMember ? PRIMITIVE_MEMBER_OF_COMPOSITE_KEY : PROP;
	}

	return new PropertyMetadata.Builder(propName, javaType, nullable).type(propertyCategory).hibType(hibernateType).columns(getPropColumns(field, mapTo, hibernateType)).build();
    }

    private PropertyMetadata getVirtualPropInfoForDynamicEntityKey(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	return new PropertyMetadata.Builder("key", String.class, true).expression(dmeg.getVirtualKeyPropForEntityWithCompositeKey(entityType)).hibType(Hibernate.STRING).type(VIRTUAL_OVERRIDE).build();
    }


    private PropertyMetadata getCalculatedPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
	final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(calculatedPropfield.getAnnotation(Calculated.class).category());

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
	return new PropertyMetadata.Builder(field.getName(), determinePropertyType(entityType, field.getName()), false).type(COLLECTIONAL).build();
    }

    private MapEntityTo getMapEntityTo(final Class entityType) {
	return getAnnotation(MapEntityTo.class, entityType);
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

	final MapEntityTo mapEntityToAnnotation = AnnotationReflector.getAnnotation(MapEntityTo.class, entityType);

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