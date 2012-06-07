package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import ua.com.fielden.platform.dao.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Injector;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getCalculatedProperties;
import static ua.com.fielden.platform.utils.EntityUtils.getCollectionalProperties;
import static ua.com.fielden.platform.utils.EntityUtils.getCompositeKeyProperties;
import static ua.com.fielden.platform.utils.EntityUtils.getPersistedProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyPartOfKey;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyRequired;



/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class DomainPersistenceMetadata {
    public final static List<String> specialProps = Arrays.asList(new String[] { AbstractEntity.ID, AbstractEntity.KEY, AbstractEntity.VERSION });
    private final static PropertyColumn id = new PropertyColumn("_ID");
    private final static PropertyColumn version = new PropertyColumn("_VERSION");
    private final static PropertyColumn key = new PropertyColumn("KEY_");
    private final static PropertyPersistenceInfo idProperty = new PropertyPersistenceInfo.Builder(AbstractEntity.ID, Long.class, false).column(id).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.ID).build();
    private final static PropertyPersistenceInfo idPropertyInOne2One = new PropertyPersistenceInfo.Builder(AbstractEntity.ID, Long.class, false).column(id).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.ONE2ONE_ID).build();
    private final static PropertyPersistenceInfo versionProperty = new PropertyPersistenceInfo.Builder(AbstractEntity.VERSION, Long.class, false).column(version).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.VERSION).build();
    private final Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> hibTypeInfosMap = new HashMap<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata>();
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final Map<Class, Object> hibTypesDefaults = new HashMap<Class, Object>();
    private Injector hibTypesInjector;

    public DomainPersistenceMetadata(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity<?>>> entityTypes) {
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
		hibTypeInfosMap.put(entityType, generateEntityPersistenceMetadata(entityType));
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalStateException("Couldn't generate persistence metadata for entity [" + entityType + "] due to: " + e);
	    }
	}
    }

    public <ET extends AbstractEntity<?>> EntityPersistenceMetadata generateEntityPersistenceMetadata(final Class<ET> entityType) throws Exception {

	final String tableClase = getTableClause(entityType);
	if (tableClase != null) {
		final Set<PropertyPersistenceInfo> ppis = generateEntityPersistenceInfo(entityType);
		final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
		result.addAll(ppis);
		result.addAll(generatePPIsForCompositeTypeProps(ppis));

	    return new EntityPersistenceMetadata(tableClase, entityType, getMap(result));
	}

	final EntityResultQueryModel<ET> entityModel = getEntityModel(entityType);
	if (entityModel != null) {
	    return new EntityPersistenceMetadata(entityModel, entityType, getMap(Collections.<PropertyPersistenceInfo> emptySet()));
	}


	final Set<PropertyPersistenceInfo> ppis = new HashSet<PropertyPersistenceInfo>();
	for (final Field field : getPersistedProperties(entityType)) {
		ppis.add(getCommonPropHibInfo(entityType, field));
	}

	return new EntityPersistenceMetadata(tableClase, entityType, getMap(ppis));

	//throw new IllegalStateException("Couldn't detemine data source for entity type: " + entityType);
    }

    private Set<PropertyPersistenceInfo> generatePPIsForCompositeTypeProps(final Set<PropertyPersistenceInfo> ppis) {
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	for (final PropertyPersistenceInfo ppi : ppis) {
	    if (!ppi.isCalculated()) {
		result.addAll(ppi.getCompositeTypeSubprops());
		result.addAll(ppi.getComponentTypeSubprops());
	    }
	}
	return result;
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

    private SortedMap<String, PropertyPersistenceInfo> getMap(final Set<PropertyPersistenceInfo> collection) {
	final SortedMap<String, PropertyPersistenceInfo> result = new TreeMap<String, PropertyPersistenceInfo>();
	for (final PropertyPersistenceInfo propertyPersistenceInfo : collection) {
	    result.put(propertyPersistenceInfo.getName(), propertyPersistenceInfo);
	}
	return result;
    }

    private boolean isOneToOne(final Class<? extends AbstractEntity<?>> entityType) {
	return AbstractEntity.class.isAssignableFrom(getKeyType(entityType));
    }

    /**
     * Generates persistence info for common properties of provided entity type.
     * @param entityType
     * @return
     * @throws Exception
     */
    private Set<PropertyPersistenceInfo> generateEntityPersistenceInfo(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	final Map<String, PropertyPersistenceInfo> result = new HashMap<String, PropertyPersistenceInfo>();
	safeMapAdd(result, isOneToOne(entityType) ? idPropertyInOne2One : idProperty);
	safeMapAdd(result, versionProperty);

	final PropertyColumn keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? new PropertyColumn(getMapEntityTo(entityType).keyColumn()) : key;

	if (isOneToOne(entityType)) {
	    safeMapAdd(result, new PropertyPersistenceInfo.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(id).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.ENTITY_KEY).build());
	} else if (!DynamicEntityKey.class.equals(getKeyType(entityType))) {
	    safeMapAdd(result, new PropertyPersistenceInfo.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic(getKeyType(entityType).getName())).type(PropertyPersistenceType.PRIMITIVE_KEY).build());
	} else if (DynamicEntityKey.class.equals(getKeyType(entityType))) {
	    final List<Field> compositeKeyProperties = getCompositeKeyProperties(entityType);
	    for (final Field field : compositeKeyProperties) {
		safeMapAdd(result, getCompositeKeyPropInfo(entityType, field));
	    }
	    safeMapAdd(result, getVirtualPropInfoForDynamicEntityKey(compositeKeyProperties));
	}

	final List<String> propsToBeSkipped = new ArrayList<String>();

	for (final PropertyPersistenceInfo propertyPersistenceInfo : result.values()) {
	    propsToBeSkipped.add(propertyPersistenceInfo.getName());
	}

	for (final Field field : getPersistedProperties(entityType)) {
	    if (!propsToBeSkipped.contains(field.getName())) {
		safeMapAdd(result, getCommonPropHibInfo(entityType, field));
	    }
	}

	for (final Field field : getCalculatedProperties(entityType)) {
	    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
	}

	for (final Field field : getCollectionalProperties(entityType)) {
	    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
	}

	return new HashSet<PropertyPersistenceInfo>(result.values());
    }

    private void safeMapAdd(final Map<String, PropertyPersistenceInfo> map, final PropertyPersistenceInfo addedItem) {
	if (!map.containsKey(addedItem.getName())) {
	    map.put(addedItem.getName(), addedItem);
	} else {
	    throw new IllegalStateException("Trying to generate duplicate PropertyPersistenceInfo " + addedItem + " for already existing " + map.get(addedItem.getName()));
	}
    }


    /**
     * Generates list of column names for mapping of CompositeUserType implementors.
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

    private boolean isRequired(final Class entityType, final String propName) {
	return isPropertyPartOfKey(entityType, propName) || isPropertyRequired(entityType, propName);
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

    private PropertyPersistenceInfo getCommonPropHibInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
	final boolean isEntity = isPersistedEntityType(field.getType());
	final boolean isUnionEntity = AbstractUnionEntity.class.isAssignableFrom(field.getType());
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final PersistedType persistedType = getPersistedType(entityType, field.getName());
	final String propName = field.getName();
	final Class javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final boolean nullable = !isRequired(entityType, propName);

	final Object hibernateType = getHibernateType(javaType, persistedType, isEntity);
	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType, nullable);

	if (isEntity) {
	    builder.type(PropertyPersistenceType.ENTITY);
	}

	if (isUnionEntity) {
	    builder.type(PropertyPersistenceType.UNION_ENTITY);
	}

	builder.hibType(hibernateType);

	return builder.columns(getPropColumns(field, mapTo, hibernateType)).build();
    }

    private String getKeyMemberConcatenationExpression(final Field keyMember) {
	if (EntityUtils.isEntityType(keyMember.getType())) {
	    return keyMember.getName() + ".key";
	} else {
	    return keyMember.getName();
	}
    }

    private PropertyPersistenceInfo getVirtualPropInfoForDynamicEntityKey(final List<Field> keyMembers) throws Exception {
	final Iterator<Field> iterator = keyMembers.iterator();
	IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> expressionModel = expr().concat().prop(getKeyMemberConcatenationExpression(iterator.next()));
	for (; iterator.hasNext();) {
	    expressionModel = expressionModel.with().val(DynamicEntityKey.KEY_MEMBERS_SEPARATOR);
	    expressionModel = expressionModel.with().prop(getKeyMemberConcatenationExpression(iterator.next()));
	}
	return new PropertyPersistenceInfo.Builder("key", String.class, true).expression(expressionModel.end().model()).hibType(Hibernate.STRING).virtual(true).build();
    }

    private PropertyPersistenceInfo getCalculatedPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
	final boolean aggregatedExpression = CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(calculatedPropfield.getAnnotation(Calculated.class).category());

	final Class javaType = determinePropertyType(entityType, calculatedPropfield.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final PersistedType persistedType = getPersistedType(entityType, calculatedPropfield.getName());
	final Object hibernateType = getHibernateType(javaType, persistedType, false);

	final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(entityType, calculatedPropfield);
	expressionModel.setContextPrefixNeeded(needsContextPrefix(entityType, calculatedPropfield));
	return new PropertyPersistenceInfo.Builder(calculatedPropfield.getName(), calculatedPropfield.getType(), true).expression(expressionModel).hibType(hibernateType).type(PropertyPersistenceType.CALCULATED).aggregatedExpression(aggregatedExpression).build();
    }

    private PropertyPersistenceInfo getCollectionalPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
	final boolean isEntity = isPersistedEntityType(field.getType());
	final String propName = field.getName();
	final Class javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;

	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType, false).type(PropertyPersistenceType.COLLECTIONAL);

	return builder.build();
    }

    private PropertyPersistenceInfo getCompositeKeyPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
	final boolean isEntity = isPersistedEntityType(field.getType());
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final PersistedType persistedType = getPersistedType(entityType, field.getName());
	final String propName = field.getName();
	final Class javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final boolean nullable = !isRequired(entityType, propName);

	final Object hibernateType = getHibernateType(javaType, persistedType, isEntity);
	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType, nullable);
	builder.type(isEntity ? PropertyPersistenceType.ENTITY_MEMBER_OF_COMPOSITE_KEY : PropertyPersistenceType.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY);
	builder.hibType(hibernateType);

	return builder.columns(getPropColumns(field, mapTo, hibernateType)).build();
    }

    private boolean needsContextPrefix(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) {
	final Calculated calcAnnotation = calculatedPropfield.getAnnotation(Calculated.class);
	//return "".equals(calcAnnotation.value()) || !AnnotationReflector.isContextual(calcAnnotation);
	// FIXME need to get full picture with collectional calculated props in order to eliminate at all.
	return true;
    }

    private ExpressionModel extractExpressionModelFromCalculatedProperty(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
	final Calculated calcAnnotation = calculatedPropfield.getAnnotation(Calculated.class);
	if (!"".equals(calcAnnotation.value())) {
	    return createExpressionText2ModelConverter(entityType, calcAnnotation).convert().getModel();
	} else {
	    try {
		final Field exprField = entityType.getDeclaredField(calculatedPropfield.getName() + "_");
		exprField.setAccessible(true);
		return (ExpressionModel) exprField.get(null);
	    } catch (final Exception e) {
		throw new IllegalStateException("Hard-coded expression model for prop [" + calculatedPropfield.getName() + "] is missing!");
	    }
	}
    }

    private ExpressionText2ModelConverter createExpressionText2ModelConverter(final Class<? extends AbstractEntity<?>> entityType, final Calculated calcAnnotation) throws Exception {
	if (AnnotationReflector.isContextual(calcAnnotation)) {
	    return new ExpressionText2ModelConverter(getRootType(calcAnnotation), calcAnnotation.contextPath(), calcAnnotation.value());
	} else {
	    return new ExpressionText2ModelConverter(entityType, calcAnnotation.value());
	}
    }

    private Class<? extends AbstractEntity<?>> getRootType(final Calculated calcAnnotation) throws ClassNotFoundException {
	return (Class<? extends AbstractEntity<?>>) ClassLoader.getSystemClassLoader().loadClass(calcAnnotation.rootTypeName());
    }

    private MapEntityTo getMapEntityTo(final Class entityType) {
	return getAnnotation(MapEntityTo.class, entityType);
    }

    private MapTo getMapTo(final Class entityType, final String propName) {
	return getPropertyAnnotation(MapTo.class, entityType, propName);
    }

    private PersistedType getPersistedType(final Class entityType, final String propName) {
	return getPropertyAnnotation(PersistedType.class, entityType, propName);
    }

    private Calculated getCalculatedPropExpression(final Class entityType, final String propName) {
	return getPropertyAnnotation(Calculated.class, entityType, propName);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> getHibTypeInfosMap() {
        return hibTypeInfosMap;
    }

    private <ET extends AbstractEntity<?>> EntityResultQueryModel<ET> getEntityModel(final Class<ET> entityType) {
	return null;
    }

    private String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
	if (!EntityUtils.isPersistedEntityType(entityType)) {
	    return null;
	    //throw new IllegalArgumentException("Trying to determine table name for not-persisted entity type [" + entityType + "]");
	}

	final MapEntityTo mapEntityToAnnotation = AnnotationReflector.getAnnotation(MapEntityTo.class, entityType);

//	if (mapEntityToAnnotation == null) {
//	    return null;
//	} else {
	    final String providedTableName = mapEntityToAnnotation.value();
	    if (!StringUtils.isEmpty(providedTableName)) {
		return providedTableName;
	    } else {
		return DynamicEntityClassLoader.getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
	    }
//	}
    }

    public Map<Class, Object> getHibTypesDefaults() {
        return hibTypesDefaults;
    }
}