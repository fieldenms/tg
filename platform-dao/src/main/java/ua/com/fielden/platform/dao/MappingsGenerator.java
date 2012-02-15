package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private final Map<Class, Class> hibTypesDefaults = new HashMap<Class, Class>();
    private Injector hibTypesInjector;
    private final DdlGenerator ddlGenerator = new DdlGenerator();

    private Map<Class, Map<String, HibTypeInfo>> hibTypeInfos = new HashMap<Class, Map<String, HibTypeInfo>>();
    final List<Class<? extends AbstractEntity>> entityTypes;
    class HibTypeInfo {
	Type hibTypeInstance;
	IUserTypeInstantiate hibUserTypeInstantiator;
	ICompositeUserTypeInstantiate hibCompositeUserTypeInstantiator;
	Class javaType;
	List<String> columns;

	protected HibTypeInfo(final Type hibTypeInstance, final IUserTypeInstantiate hibUserTypeInstantiator, final ICompositeUserTypeInstantiate hibCompositeUserTypeInstantiator) {
	    this.hibTypeInstance = hibTypeInstance;
	    this.hibCompositeUserTypeInstantiator = hibCompositeUserTypeInstantiator;
	    this.hibUserTypeInstantiator = hibUserTypeInstantiator;
	}

	public HibTypeInfo(final Type hibTypeInstance) {
	    this(hibTypeInstance, null, null);
	}


	public HibTypeInfo(final IUserTypeInstantiate hibUserTypeInstantiator) {
	    this(null, hibUserTypeInstantiator, null);
	}

	public HibTypeInfo(final ICompositeUserTypeInstantiate hibCompositeUserTypeInstantiator) {
	    this(null, null, hibCompositeUserTypeInstantiator);
	}

    }

    public MappingsGenerator(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector, final List<Class<? extends AbstractEntity>> entityTypes) {
	if (hibTypesDefaults != null) {
	    this.hibTypesDefaults.putAll(hibTypesDefaults);
	}
	this.hibTypesInjector = hibTypesInjector;
	this.entityTypes = entityTypes;
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
		hibTypeInfos.put(entityType, getEntityTypeInfo(entityType));

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

    private String getPropMappingString(final String propName, final List<String> propColumns, final String hibTypeName, final String length) {
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

    private String getPropMappingString(final String propName, final List<String> propColumns, final String hibTypeName) {
	return getPropMappingString(propName, propColumns, hibTypeName, null);
    }

    private String getPropMappingString(final String propName, final String propColumn, final String hibTypeName, final String length) {
	return getPropMappingString(propName, Arrays.asList(new String[] { propColumn }), hibTypeName, length);
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

    private String getClassMappingPartForCommonProps(final Class entityType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	for (final Field field : getPersistedProperties(entityType)) {
	    if (!specialProps.contains(field.getName())) {
		sb.append(getCommonPropMappingString(entityType, field));
	    }
	}
	return sb.toString();
    }

    private String getCommonPropMappingString(final Class entityType, final Field field) throws Exception {
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";

	if (Set.class.isAssignableFrom(field.getType())) {
	    return getSet(field.getName(), columnName, determinePropertyType(entityType, field.getName()));
	} else if (isPersistedEntityType(field.getType())) {
	    return getManyToOneProperty(field.getName(), columnName, field.getType());
	} else if (isNotEmpty(mapTo.typeName())) {
	    return getPropMappingString(field.getName(), columnName, mapTo.typeName());
	} else {
	    final String length = (field.getType().isArray() && field.getType().getComponentType().isAssignableFrom(byte.class)) ? "1073741824" : null;
	    final Class hibType = hibTypesInjector != null && !mapTo.userType().equals(Class.class) ? hibTypesInjector.getInstance(mapTo.userType()).getClass() : //
		    hibTypesDefaults.get(field.getType());
	    if (hibType == null) {
		return getPropMappingString(field.getName(), columnName, null, length);
	    } else {
		if (!ICompositeUserTypeInstantiate.class.isAssignableFrom(hibType)) {
		    return getPropMappingString(field.getName(), columnName, hibType.getName(), length);
		} else {
		    return getPropMappingString(field.getName(), getCompositeUserTypeColumns(hibType, columnName), hibType.getName(), length);
		}
	    }
	}
    }

    private String getCommonPropHibInfo(final Class entityType, final Field field) throws Exception {
	final MapTo mapTo = getMapTo(entityType, field.getName());
	final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";

	if (Set.class.isAssignableFrom(field.getType())) {
	    return getSet(field.getName(), columnName, determinePropertyType(entityType, field.getName()));
	} else if (isPersistedEntityType(field.getType())) {
	    return getManyToOneProperty(field.getName(), columnName, field.getType());
	} else if (isNotEmpty(mapTo.typeName())) {
	    return getPropMappingString(field.getName(), columnName, mapTo.typeName());
	} else {
	    final String length = (field.getType().isArray() && field.getType().getComponentType().isAssignableFrom(byte.class)) ? "1073741824" : null;
	    final Class hibType = hibTypesInjector != null && !mapTo.userType().equals(Class.class) ? hibTypesInjector.getInstance(mapTo.userType()).getClass() : //
		    hibTypesDefaults.get(field.getType());
	    if (hibType == null) {
		return getPropMappingString(field.getName(), columnName, null, length);
	    } else {
		if (!ICompositeUserTypeInstantiate.class.isAssignableFrom(hibType)) {
		    return getPropMappingString(field.getName(), columnName, hibType.getName(), length);
		} else {
		    return getPropMappingString(field.getName(), getCompositeUserTypeColumns(hibType, columnName), hibType.getName(), length);
		}
	    }
	}
    }



    private Map<String, HibTypeInfo> getEntityTypeInfo(final Class<? extends AbstractEntity> entityType) {
	// TODO Auto-generated method stub
	return null;
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
