package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.type.BooleanType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.persistence.DdlGenerator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
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
public class MappingsGenerator {
    private static List<String> specialProps = Arrays.asList(new String[] { "id", "key", "version" });

    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final Map<Class, Class> hibTypesDefaults = new HashMap<Class, Class>();
    private Injector hibTypesInjector;
    private final DdlGenerator ddlGenerator = new DdlGenerator();

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

    /**
     * Just for convenience of search.
     */
    private Map<Class, SortedMap<String, PropertyPersistenceInfo>> hibTypeInfosMap = new HashMap<Class, SortedMap<String, PropertyPersistenceInfo>>();
    final List<Class<? extends AbstractEntity>> entityTypes;

    public String getTableClause(final Class entityType) {
	return ddlGenerator.getTableClause(entityType);
    }

    /**
     * Retrieves persistence info for entity property, which is explicitly persisted within this entity type.
     * @param entityType
     * @param propName
     * @return
     */
    public PropertyPersistenceInfo getPropPersistenceInfoExplicitly(final Class entityType, final String propName) {
	final SortedMap<String,PropertyPersistenceInfo> map = hibTypeInfosMap.get(entityType);
	return map != null ? map.get(propName) : null;
    }

    /**
     * Retrieves persistence info for entity property or its nested subproperty.
     * @param entityType
     * @param propName
     * @return
     */
    public PropertyPersistenceInfo getInfoForDotNotatedProp(final Class entityType, final String dotNotatedPropName) {
	final PropertyPersistenceInfo simplePropInfo = getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
	if (simplePropInfo != null) {
	    return simplePropInfo;
	} else {
	    final Pair<String, String> propSplit = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	    final PropertyPersistenceInfo firstPropInfo = getPropPersistenceInfoExplicitly(entityType, propSplit.getKey());
	    if (firstPropInfo != null && firstPropInfo.getJavaType() != null) {
		return getInfoForDotNotatedProp(firstPropInfo.getJavaType(), propSplit.getValue());
	    } else {
		return null;
	    }
	}
    }

    public boolean isNullable (final Class entityType, final String dotNotatedPropName) {
	final PropertyPersistenceInfo simplePropInfo = getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
	if (simplePropInfo != null) {
	    return simplePropInfo.isNullable();
	} else {
	    final Pair<String, String> propSplit = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	    final PropertyPersistenceInfo firstPropInfo = getPropPersistenceInfoExplicitly(entityType, propSplit.getKey());
	    if (firstPropInfo != null && firstPropInfo.getJavaType() != null) {
		return isNullable(firstPropInfo.getJavaType(), propSplit.getValue()) || firstPropInfo.isNullable();
	    } else {
		throw new IllegalArgumentException("Couldn't determine nullability for prop [" + dotNotatedPropName + "] in type [" + entityType + "]" );
	    }
	}
    }

    public MappingsGenerator(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity>> entityTypes) {
	if (hibTypesDefaults != null) {
	    this.hibTypesDefaults.putAll(hibTypesDefaults);
	}
	this.hibTypesInjector = hibTypesInjector;
	this.entityTypes = entityTypes;
	for (final Class<? extends AbstractEntity> entityType : entityTypes) {
	    try {
		final Set<PropertyPersistenceInfo> ppis = generateEntityPersistenceInfo(entityType);
		final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
		result.addAll(ppis);
		result.addAll(generatePPIsForCompositeTypeProps(ppis));
		hibTypeInfosMap.put(entityType, getMap(result));
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
    }

    private SortedMap<String, PropertyPersistenceInfo> getMap(final Set<PropertyPersistenceInfo> collection) {
	final SortedMap<String, PropertyPersistenceInfo> result = new TreeMap<String, PropertyPersistenceInfo>();
	for (final PropertyPersistenceInfo propertyPersistenceInfo : collection) {
	    result.put(propertyPersistenceInfo.getName(), propertyPersistenceInfo);
	}
	return result;
    }

    public Collection<PropertyPersistenceInfo> getEntityPPIs(final Class entityType) {
	return hibTypeInfosMap.get(entityType).values();
    }

    public MappingsGenerator(final List<Class<? extends AbstractEntity>> entityTypes) {
	this(null, null, entityTypes);
    }

    public String generateMappings() {
	final StringBuffer sb = new StringBuffer();
	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
	sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
	sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
	sb.append("<hibernate-mapping default-access=\"field\">\n");

	for (final Class<? extends AbstractEntity> entityType : entityTypes) {
	    try {
		sb.append(getClassMapping(entityType));
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new RuntimeException("Couldn't generate mapping for " + entityType.getName() + " due to: " + e.getMessage());
	    }
	    sb.append("\n");
	}
	sb.append("</hibernate-mapping>");

	final String result = sb.toString();
	System.out.println(result);
	return result;
    }

    private String getCommonEntityId(final String name, final String column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"" + name + "\" column=\"" + column + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
	sb.append("\t\t<generator class=\"hilo\">\n");
	sb.append("\t\t\t<param name=\"table\">UNIQUE_ID</param>\n");
	sb.append("\t\t\t<param name=\"column\">NEXT_VALUE</param>\n");
	sb.append("\t\t\t<param name=\"max_lo\">0</param>\n");
	sb.append("\t\t</generator>\n");
	sb.append("\t</id>\n");
	return sb.toString();
    }

    private String getOneToOneEntityId(final String name, final String column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"" + name + "\" column=\"" + column + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
	sb.append("\t\t<generator class=\"foreign\">\n");
	sb.append("\t\t\t<param name=\"property\">key</param>\n");
	sb.append("\t\t</generator>\n");
	sb.append("\t</id>\n");

	return sb.toString();
    }

    private String getSet(final String propName, final String propColumn, final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<set name=\"" + propName + "\">\n");
	sb.append("\t\t<key column=\"" + propColumn + "\"/>\n");
	sb.append("\t\t<one-to-many  class=\"" + entityType.getName() + "\"/>\n");
	sb.append("\t</set>\n");
	return sb.toString();
    }

    private String getCommonEntityVersion(final String name, final String column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<version name=\"" + name + "\" type=\"" + hibTypeName + "\" access=\"field\" insert=\"false\">\n");
	sb.append("\t\t<column name=\"" + column + "\" default=\"0\" />\n");
	sb.append("\t</version>\n");
	return sb.toString();
    }

    private String getManyToOneProperty(final String propName, final String propColumn, final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + propColumn + "\"");
	sb.append(isOneToOne(entityType) ? " unique=\"true\" insert=\"false\" update=\"false\"" : "");
	sb.append("/>\n");
	return sb.toString();
    }

    private String getOneToOneProperty(final String propName, final Class entityType) {
	return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }

    private String getPropMappingString(final String propName, final List<String> propColumns, final String hibTypeName, final Long length) {
	final String propNameClause = "\t<property name=\"" + propName + "\"";
	final String typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
	final String lengthClause = length == null ? "" : " length=\"" + length + "\"";
	final String endClause = "/>\n";
	if (propColumns.size() == 1) {
	    final String columnClause = " column=\"" + propColumns.get(0) + "\"";
	    return propNameClause + columnClause + typeClause + lengthClause + endClause;
	} else {
	    final StringBuffer sb = new StringBuffer();
	    sb.append(propNameClause + typeClause + ">\n");
	    for (final String column : propColumns) {
		sb.append("\t\t<column name=\"" + column + "\"" + endClause);
	    }
	    sb.append("\t</property>\n");
	    return sb.toString();
	}
    }

    private boolean isOneToOne(final Class entityType) {
	return AbstractEntity.class.isAssignableFrom(getKeyType(entityType));
    }

    /**
     * Generates mapping for entity type.
     *
     * @param entityType
     * @return
     * @throws Exception
     */
    private String getClassMapping(final Class entityType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	sb.append("<class name=\"" + entityType.getName() + "\" table=\"" + getTableClause(entityType) + "\">\n");

	final Map<String, PropertyPersistenceInfo> map = hibTypeInfosMap.get(entityType);
	sb.append(getCommonPropMappingString(map.get("id")));
	sb.append(getCommonPropMappingString(map.get("version")));
	if (map.get("key") != null) {
	    sb.append(getCommonPropMappingString(map.get("key")));
	}

	for (final PropertyPersistenceInfo ppi : map.values()) {
	    if (!ppi.getType().equals(PropertyPersistenceType.COMPOSITE_DETAILS) && !specialProps.contains(ppi.getName())) {
		sb.append(getCommonPropMappingString(ppi));
	    }
	}
	sb.append("</class>\n");
	return sb.toString();
    }

    /**
     * Generates persistence info for common properties of provided entity type.
     * @param entityType
     * @return
     * @throws Exception
     */
    private Set<PropertyPersistenceInfo> generateEntityPersistenceInfo(final Class entityType) throws Exception {
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	result.add(new PropertyPersistenceInfo.Builder("id", Long.class, false).column(ddlGenerator.id).hibType(TypeFactory.basic("long")).type(isOneToOne(entityType) ? PropertyPersistenceType.ONE2ONE_ID : PropertyPersistenceType.ID).build());
	result.add(new PropertyPersistenceInfo.Builder("version", Long.class, false).column(ddlGenerator.version).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.VERSION).build());

	final String keyColumnOverride = isNotEmpty(getMapEntityTo(entityType).keyColumn()) ? getMapEntityTo(entityType).keyColumn() : ddlGenerator.key;
	if (isOneToOne(entityType)) {
	    result.add(new PropertyPersistenceInfo.Builder("key", getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic("long")).type(PropertyPersistenceType.ENTITY_KEY).build());
	} else if (!DynamicEntityKey.class.equals(getKeyType(entityType))){
	    result.add(new PropertyPersistenceInfo.Builder("key", getKeyType(entityType), false).column(keyColumnOverride).hibType(TypeFactory.basic(getKeyType(entityType).getName())).type(PropertyPersistenceType.PRIMITIVE_KEY).build());
	}

	for (final Field field : getPersistedProperties(entityType)) {
	    if (!specialProps.contains(field.getName())) {
		final PropertyPersistenceInfo ppi = getCommonPropHibInfo(entityType, field);
		result.add(ppi);
	    }
	}
	return result;
    }

    private Set<PropertyPersistenceInfo> generatePPIsForCompositeTypeProps(final Set<PropertyPersistenceInfo> ppis) {
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	for (final PropertyPersistenceInfo ppi : ppis) {
	    result.addAll(ppi.getCompositeTypeSubprops());
	}
	return result;
    }

    /**
     * Generates mapping string for common property based on it persistence info.
     * @param info
     * @return
     * @throws Exception
     */
    private String getCommonPropMappingString(final PropertyPersistenceInfo info) throws Exception {
	switch (info.getType()) {
	case ENTITY_KEY:
	    return getOneToOneProperty(info.getName(), info.getJavaType());
	case COLLECTIONAL:
	    return getSet(info.getName(), info.getColumn(), info.getJavaType());
	case ENTITY:
	    return getManyToOneProperty(info.getName(), info.getColumn(), info.getJavaType());
	case VERSION:
	    return getCommonEntityVersion(info.getName(), info.getColumn(), info.getTypeString());
	case ID:
	    return getCommonEntityId(info.getName(), info.getColumn(), info.getTypeString());
	case ONE2ONE_ID:
	    return getOneToOneEntityId(info.getName(), info.getColumn(), info.getTypeString());
	default:
	    return getPropMappingString(info.getName(), info.getColumns(), info.getTypeString(), info.getLength());
	}
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

	if (hibTypesInjector != null && !hibernateUserTypeImplementor.equals(Class.class)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
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

    protected boolean isRequired(final Class entityType, final String propName) {
	return isPropertyPartOfKey(entityType, propName) || isPropertyRequired(entityType, propName);
    }

    /**
     * Generates persistence info for entity property.
     * @param entityType
     * @param field
     * @return
     * @throws Exception
     */
    private PropertyPersistenceInfo getCommonPropHibInfo(final Class entityType, final Field field) throws Exception {
	final boolean isCollectional = Set.class.isAssignableFrom(field.getType());
	final boolean isEntity = isPersistedEntityType(field.getType());
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final String propName = field.getName();
	final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";
	final Class javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
	final long length = mapTo.length();
	final boolean nullable = !isRequired(entityType, propName);

	final Object hibernateType = getHibernateType(javaType, mapTo.typeName(), mapTo.userType(), isCollectional, isEntity);
	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType, nullable).length(length);
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

    private MapEntityTo getMapEntityTo(final Class entityType) {
	return getAnnotation(MapEntityTo.class, entityType);
    }

    private MapTo getMapTo(final Class entityType, final String propName) {
	return getPropertyAnnotation(MapTo.class, entityType, propName);
    }
}