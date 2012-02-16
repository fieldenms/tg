package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.usertype.CompositeUserType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.persistence.DdlGenerator;

import com.google.inject.Injector;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getPersistedProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

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

    private Map<Class, SortedMap<String, PropertyPersistenceInfo>> hibTypeInfos = new HashMap<Class, SortedMap<String, PropertyPersistenceInfo>>();
    final List<Class<? extends AbstractEntity>> entityTypes;

    public MappingsGenerator(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity>> entityTypes) {
	if (hibTypesDefaults != null) {
	    this.hibTypesDefaults.putAll(hibTypesDefaults);
	}
	this.hibTypesInjector = hibTypesInjector;
	this.entityTypes = entityTypes;
	for (final Class<? extends AbstractEntity> entityType : entityTypes) {
	    try {
		hibTypeInfos.put(entityType, generateEntityPersistenceInfo(entityType));
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
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

    private String getCommonEntityId() {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"id\" column=\"" + ddlGenerator.id + "\" type=\"long\" access=\"property\">\n");
	sb.append("\t\t<generator class=\"hilo\">\n");
	sb.append("\t\t\t<param name=\"table\">UNIQUE_ID</param>\n");
	sb.append("\t\t\t<param name=\"column\">NEXT_VALUE</param>\n");
	sb.append("\t\t\t<param name=\"max_lo\">0</param>\n");
	sb.append("\t\t</generator>\n");
	sb.append("\t</id>\n");
	return sb.toString();
    }

    private String getOneToOneEntityId() {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"id\" column=\"" + ddlGenerator.id + "\" type=\"long\" access=\"property\">\n");
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

    private String getCommonEntityVersion() {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<version name=\"version\" type=\"long\" access=\"field\" insert=\"false\">\n");
	sb.append("\t\t<column name=\"" + ddlGenerator.version + "\" default=\"0\" />\n");
	sb.append("\t</version>\n");
	return sb.toString();
    }

    private String getSimpleKey(final Class entityType) {
	return getSimpleKeyWithColumn(ddlGenerator.key, entityType);
    }

    private String getSimpleKeyWithColumn(final String column, final Class entityType) {
	return getPropMappingString("key", column, TypeFactory.basic(getKeyType(entityType).getName()).getName());
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

    private String getPropMappingString(final String propName, final String propColumn, final String hibTypeName) {
	return getPropMappingString(propName, Arrays.asList(new String[] { propColumn }), hibTypeName, null);
    }

    private List<String> getCompositeUserTypeColumns(final Class<? extends ICompositeUserTypeInstantiate> hibTypeClass, final String parentColumn) throws Exception {
	final CompositeUserType hibType = (CompositeUserType) hibTypeClass.newInstance();
	final Class rc = hibType.returnedClass();
	final String[] propNames = hibType.getPropertyNames();
	final List<String> result = new ArrayList<String>();
	for (final String propName : propNames) {
	    final String mapToColumn = getMapTo(rc, propName).value();
	    final String columnName = propNames.length == 1 ? parentColumn
		    : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
	    result.add(columnName);
	}
	return result;
    }

    private boolean isOneToOne(final Class entityType) {
	return AbstractEntity.class.isAssignableFrom(getKeyType(entityType));
    }

    private String getKeyMappingString(final Class entityType) {
	if (DynamicEntityKey.class.equals(getKeyType(entityType))) {
	    return "";
	} else {
	    if (isOneToOne(entityType)) {
		return getOneToOneProperty("key", getKeyType(entityType));
	    } else {
		final String keyColumnOverride = getMapEntityTo(entityType).keyColumn();
		if (isNotEmpty(keyColumnOverride)) {
		    return getSimpleKeyWithColumn(keyColumnOverride, entityType);
		} else {
		    return getSimpleKey(entityType);
		}
	    }
	}
    }

    /**
     * Generates mapping for entity type.
     * @param entityType
     * @return
     * @throws Exception
     */
    private String getClassMapping(final Class entityType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	sb.append("<class name=\"" + entityType.getName() + "\" table=\"" + ddlGenerator.getTableClause(entityType) + "\">\n");
	sb.append(isOneToOne(entityType) ? getOneToOneEntityId() : getCommonEntityId());
	sb.append(getCommonEntityVersion());
	sb.append(getKeyMappingString(entityType));
	sb.append(getClassMappingPartForCommonProps(entityType));
	sb.append("</class>\n");
	return sb.toString();
    }

    /**
     * Generates mappings for entity common props.
     * @param entityType
     * @return
     * @throws Exception
     */
    private String getClassMappingPartForCommonProps(final Class entityType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	for (final PropertyPersistenceInfo ppi : hibTypeInfos.get(entityType).values()) {
		sb.append(getCommonPropMappingString(ppi));
	}
	return sb.toString();
    }

    /**
     * Generates persistence info for common properties of provided entity type.
     * @param entityType
     * @return
     * @throws Exception
     */
    private SortedMap<String, PropertyPersistenceInfo> generateEntityPersistenceInfo(final Class entityType) throws Exception {
	final SortedMap<String, PropertyPersistenceInfo> result = new TreeMap<String, PropertyPersistenceInfo>();
	for (final Field field : getPersistedProperties(entityType)) {
	    if (!specialProps.contains(field.getName())) {
		final PropertyPersistenceInfo ppi = getCommonPropHibInfo(entityType, field);
		result.put(ppi.getName(), ppi);
	    }
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
	if (info.isCollection()) {
	    return getSet(info.getName(), info.getColumn(), info.getJavaType());
	}

	if (info.isEntity()) {
	    return getManyToOneProperty(info.getName(), info.getColumn(), info.getJavaType());
	}

	return getPropMappingString(info.getName(), info.getColumns(), info.getTypeString(), info.getLength());
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

	final Object hibernateType = getHibernateType(javaType, mapTo.typeName(), mapTo.userType(), isCollectional, isEntity);
	final PropertyPersistenceInfo.Builder builder = new PropertyPersistenceInfo.Builder(propName, javaType).collectional(isCollectional).length(length);

	if (hibernateType != null) {
	    if (hibernateType instanceof Type) {
		builder.hibType((Type) hibernateType);
	    } else if (hibernateType instanceof IUserTypeInstantiate) {
		builder.hibUserType((IUserTypeInstantiate) hibernateType);
	    } else if (hibernateType instanceof ICompositeUserTypeInstantiate) {
		final ICompositeUserTypeInstantiate hibCompositeUSerType = (ICompositeUserTypeInstantiate) hibernateType;
		builder.hibCompositeUserType(hibCompositeUSerType);
		for(final String column : getCompositeUserTypeColumns(hibCompositeUSerType, columnName)) {
		    builder.column(column);
		}
		return builder.build();
	    }
	}

	return builder.column(columnName).build();
    }

    private MapEntityTo getMapEntityTo(final Class entityType) {
	return getAnnotation(MapEntityTo.class, entityType);
    }

    private MapTo getMapTo(final Class entityType, final String propName) {
	return getPropertyAnnotation(MapTo.class, entityType, propName);
    }

    private Class getDefaultHibType(final Class parentType, final String propName) {
	return hibTypesDefaults.get(determinePropertyType(parentType, propName));
    }

    public Type determinePropertyHibType(final Class<?> parentType, final String propName) throws Exception {
	if (EntityAggregates.class.equals(parentType) || IUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    return null;
	} else if (ICompositeUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    final CompositeUserType hibTypeInstance = (CompositeUserType) hibTypesInjector.getInstance(parentType);
	    return hibTypeInstance.getPropertyTypes()[Arrays.asList(hibTypeInstance.getPropertyNames()).indexOf(propName)];
	} else {
	    final MapTo mapTo = getMapTo(parentType, propName);
	    if (isNotEmpty(mapTo.typeName())) {
		return TypeFactory.basic(mapTo.typeName());
	    } else {
		final Class propType = determinePropertyType(parentType, propName);
		final Class hibType = hibTypesDefaults.get(propType);
		if (hibType != null) {
		    final Object hibTypeInstance = hibType.newInstance();
		    return hibTypeInstance instanceof Type ? (Type) hibTypeInstance : null;
		} else {
		    return TypeFactory.basic(propType.getName());
		}
	    }
	}
    }

    public IUserTypeInstantiate determinePropertyHibUserType(final Class<?> parentType, final String propName) throws Exception {
	if (EntityAggregates.class.equals(parentType) || IUserTypeInstantiate.class.isAssignableFrom(parentType)
		|| ICompositeUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    return null;
	} else {
	    final MapTo mapTo = getMapTo(parentType, propName);
	    if (isEmpty(mapTo.typeName())) {
		final Class hibType = getDefaultHibType(parentType, propName);
		final Object hibTypeInstance = hibType == null ? null : hibType.newInstance();
		return hibTypeInstance instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibTypeInstance : null;
	    } else {
		final Object hibTypeInstance = hibTypesInjector.getInstance(mapTo.userType());
		return hibTypeInstance instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibTypeInstance : null;
	    }
	}
    }

    public Class determinePropertyHibCompositeUserType(final Class<?> parentType, final String propName) throws Exception {
	if (EntityAggregates.class.equals(parentType)) {
	    return null;
	} else {
	    final MapTo mapTo = getMapTo(parentType, propName);
	    if (Class.class.equals(mapTo.userType())) {
		final Class hibType = getDefaultHibType(parentType, propName);
		final Object hibTypeInstance = hibType == null ? null : hibType.newInstance();
		return hibTypeInstance instanceof ICompositeUserTypeInstantiate ? hibTypeInstance.getClass() : null;
	    } else {
		final Object hibTypeInstance = hibTypesInjector.getInstance(mapTo.userType());
		return hibTypeInstance instanceof ICompositeUserTypeInstantiate ? hibTypeInstance.getClass() : null;
	    }
	}
    }
}
