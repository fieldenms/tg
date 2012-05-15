package ua.com.fielden.platform.dao;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getCalculatedProperties;
import static ua.com.fielden.platform.utils.EntityUtils.getPersistedProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyPartOfKey;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyRequired;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Injector;


/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class DomainPersistenceMetadata {
    public final static List<String> specialProps = Arrays.asList(new String[] { AbstractEntity.ID, AbstractEntity.KEY, AbstractEntity.VERSION });
    private final static String id = "_ID";
    private final static String version = "_VERSION";
    private final static String key = "KEY_";
    private final static String desc = "DESC_";

    private final Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> hibTypeInfosMap = new HashMap<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata>();
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final Map<Class, Class> hibTypesDefaults = new HashMap<Class, Class>();
    private Injector hibTypesInjector;

    public DomainPersistenceMetadata(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity<?>>> entityTypes) {
	if (hibTypesDefaults != null) {
	    this.hibTypesDefaults.putAll(hibTypesDefaults);
	}
	this.hibTypesInjector = hibTypesInjector;
	for (final Class<? extends AbstractEntity<?>> entityType : entityTypes) {
	    try {
		hibTypeInfosMap.put(entityType, generateEntityPersistenceMetadata(entityType));
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public EntityPersistenceMetadata generateEntityPersistenceMetadata(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	final Set<PropertyPersistenceInfo> ppis = generateEntityPersistenceInfo(entityType);
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	result.addAll(ppis);
	result.addAll(generatePPIsForCompositeTypeProps(ppis));
	return new EntityPersistenceMetadata(getTableClause(entityType), entityType, getMap(result));
    }

    private Set<PropertyPersistenceInfo> generatePPIsForCompositeTypeProps(final Set<PropertyPersistenceInfo> ppis) {
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	for (final PropertyPersistenceInfo ppi : ppis) {
	    if (!ppi.isCalculated()) {
		result.addAll(ppi.getCompositeTypeSubprops());
	    }
	}
	return result;
    }

    public Object getBooleanValue(final boolean value) {
	final Class booleanHibClass = hibTypesDefaults.get(boolean.class);
	if (booleanHibClass.equals(YesNoType.class)) {
	    return value ? "Y" : "N";
	}
	if (booleanHibClass.equals(TrueFalseType.class)) {
	    return value ? "T" : "F";
	}
	if (booleanHibClass.equals(BooleanType.class)) {
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
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	result.add(new PropertyPersistenceInfo.Builder(AbstractEntity.ID, Long.class, false).column(id).hibType(TypeFactory.basic("long")).type(isOneToOne(entityType) ? PropertyPersistenceType.ONE2ONE_ID : PropertyPersistenceType.ID).build());
	result.add(new PropertyPersistenceInfo.Builder(AbstractEntity.VERSION, Long.class, false).column(version).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.VERSION).build());

	final String keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? getMapEntityTo(entityType).keyColumn() : key;
	if (isOneToOne(entityType)) {
	    result.add(new PropertyPersistenceInfo.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(id).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.ENTITY_KEY).build());
	} else if (!DynamicEntityKey.class.equals(getKeyType(entityType))){
	    result.add(new PropertyPersistenceInfo.Builder(AbstractEntity.KEY, getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic(getKeyType(entityType).getName())).type(PropertyPersistenceType.PRIMITIVE_KEY).build());
	}

	for (final Field field : getPersistedProperties(entityType)) {
	    if (!specialProps.contains(field.getName())) {
		result.add(getCommonPropHibInfo(entityType, field));
	    }
	}

	for (final Field field : getCalculatedProperties(entityType)) {
	    result.add(getCalculatedPropInfo(entityType, field));
	}

	return result;
    }

    /**
     * Generates list of column names for mapping of CompositeUserType implementors.
     * @param hibType
     * @param parentColumn
     * @return
     * @throws Exception
     */
    private List<String> getCompositeUserTypeColumns(final ICompositeUserTypeInstantiate hibType, final String parentColumn) throws Exception {
	final String[] propNames = hibType.getPropertyNames();
	final List<String> result = new ArrayList<String>();
	for (final String propName : propNames) {
	    final String mapToColumn = getMapTo(hibType.returnedClass(), propName).value();
	    final String columnName = propNames.length == 1 ? parentColumn
		    : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
	    result.add(columnName);
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
    private Object getHibernateType(final Class javaType, final String hibernateTypeName, final Class hibernateUserTypeImplementor, final boolean collectional, final boolean entity) throws Exception {
	if (collectional) {
	    return null;
	}

	if (entity) {
	    return TypeFactory.basic("long");
	}

	if (isNotEmpty(hibernateTypeName)) {
	    return TypeFactory.basic(hibernateTypeName);
	}

	if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
	    return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
	} else {
	    final Class defaultHibType = hibTypesDefaults.get(javaType);
	    if (defaultHibType != null) { // default is provided for given property java type
		return defaultHibType.newInstance();
	    } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
		return TypeFactory.basic(javaType.getName());
	    }
	}
    }

    private boolean isRequired(final Class entityType, final String propName) {
	return isPropertyPartOfKey(entityType, propName) || isPropertyRequired(entityType, propName);
    }

    /**
     * Generates persistence info for entity property.
     * @param entityType
     * @param field
     * @return
     * @throws Exception
     */
    private PropertyPersistenceInfo getCommonPropHibInfo(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
	final boolean isCollectional = Set.class.isAssignableFrom(field.getType());
	final boolean isEntity = isPersistedEntityType(field.getType());
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final String propName = field.getName();
	final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";
	final Class javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final long length = mapTo.length();
	final long precision = mapTo.precision();
	final long scale = mapTo.scale();
	final boolean nullable = !isRequired(entityType, propName);

	final Object hibernateType = getHibernateType(javaType, mapTo.typeName(), mapTo.userType(), isCollectional, isEntity);
	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType, nullable).length(length).precision(precision).scale(scale);
	if (isCollectional) {
	    builder.type(PropertyPersistenceType.COLLECTIONAL);
	}

	if (isEntity) {
	    builder.type(PropertyPersistenceType.ENTITY);
	}

	builder.hibType(hibernateType);

	if (hibernateType instanceof ICompositeUserTypeInstantiate) {
	    final ICompositeUserTypeInstantiate hibCompositeUSerType = (ICompositeUserTypeInstantiate) hibernateType;
	    for (final String column : getCompositeUserTypeColumns(hibCompositeUSerType, columnName)) {
		builder.column(column);
	    }
	    return builder.build();
	}

	return builder.column(columnName).build();
    }

    private PropertyPersistenceInfo getCalculatedPropInfo(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
	final Class javaType = determinePropertyType(entityType, calculatedPropfield.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final Object hibernateType = getHibernateType(javaType, "", Void.class, false, false);

	final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(entityType, calculatedPropfield);
	expressionModel.setContextPrefixNeeded(needsContextPrefix(entityType, calculatedPropfield));
	return new PropertyPersistenceInfo.Builder(calculatedPropfield.getName(), calculatedPropfield.getType(), true).expression(expressionModel).hibType(hibernateType).build();
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

    private Calculated getCalculatedPropExpression(final Class entityType, final String propName) {
	return getPropertyAnnotation(Calculated.class, entityType, propName);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> getHibTypeInfosMap() {
        return hibTypeInfosMap;
    }

    public static String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
	if (!EntityUtils.isPersistedEntityType(entityType)) {
	    throw new IllegalArgumentException("Trying to determine table name for not-persisted entity type [" + entityType + "]");
	}
	final String providedTableName = AnnotationReflector.getAnnotation(MapEntityTo.class, entityType).value();
	if (!StringUtils.isEmpty(providedTableName)) {
	    return providedTableName;
	} else {
	    return entityType.getSimpleName().toUpperCase() + "_";
	}
    }
}