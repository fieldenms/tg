package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.usertype.CompositeUserType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.persistence.DdlGenerator;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import com.google.inject.Injector;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class MappingsGenerator {
    private static List<String> specialProps = Arrays.asList(new String[] { "id", "key", "desc", "version" });

    private final Map<Class, Class> hibTypesDefaults = new HashMap<Class, Class>();
    private Injector hibTypesInjector;
    private final DdlGenerator ddlGenerator = new DdlGenerator();

    public MappingsGenerator(final Map<Class, Class> hibTypesDefaults, final Injector hibTypesInjector) {
	this.hibTypesDefaults.putAll(hibTypesDefaults);
	this.hibTypesInjector = hibTypesInjector;
    }

    public MappingsGenerator() {}

    public String generateMappings(final List<Class<? extends AbstractEntity>> entityTypes) {
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

	System.out.println(sb.toString());
	return sb.toString();
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

    private String getSimpleKey() {
	return getSimpleKeyWithColumn(ddlGenerator.key);
    }

    private String getSimpleKeyWithColumn(final String column) {
	return getPlainProperty("key", column, "string");
    }

    private String getSimpleDesc() {
	return getPlainProperty("desc", ddlGenerator.desc, "string");
    }

    /**
     *
     * @param propName
     * @param propColumn
     * @param hibTypeName - hibernate built-in type name (e.g. string, yes_no, long)
     * @return
     */
    private String getPlainProperty(final String propName, final String propColumn, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	final String columnName = !StringUtils.isEmpty(propColumn) ? propColumn : propName.toUpperCase() + "_";
	sb.append("\t<property name=\"" + propName + "\" column=\"" + columnName + "\"");
	if (!StringUtils.isEmpty(hibTypeName)) {
		sb.append(" type=\"" + hibTypeName + "\"");
	}
	sb.append("/>\n");

	return sb.toString();
    }

    private String getPlainProperty(final String propName, final String propColumn, final Class hibType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	if (hibType == null) {
	    sb.append("\t<property name=\"" + propName + "\" column=\"" + propColumn + "\"/>\n");
	} else {
	    if (!ICompositeUserTypeInstantiate.class.isAssignableFrom(hibType)) {
		sb.append("\t<property name=\"" + propName + "\" column=\"" + propColumn + "\" type=\"" + hibType.getName() + "\"/>\n");
	    } else {
		final List<String> columns = getCompositeUserTypeColumns(hibType);
		if (columns.size() == 1) {
		    sb.append("\t<property name=\"" + propName + "\" column=\"" + propColumn + "\" type=\"" + hibType.getName() + "\"/>\n");
		} else {
		    sb.append("\t<property name=\"" + propName + "\" type=\"" + hibType.getName() + "\">\n");
		    for (final String column : columns) {
			sb.append("\t\t<column name=\"" + propColumn + (propColumn.endsWith("_") ? "" : "_") + column + "\"/>\n");
		    }
		    sb.append("\t</property>\n");
		}
	    }
	}

	return sb.toString();
    }

    private List<String> getCompositeUserTypeColumns(final Class<? extends ICompositeUserTypeInstantiate> hibTypeClass) throws Exception {
	final CompositeUserType hibType = (CompositeUserType) hibTypeClass.newInstance();
	final Class rc = hibType.returnedClass();
	final String[] propNames = hibType.getPropertyNames();
	final List<String> result = new ArrayList<String>();
	for (final String propName : propNames) {
	    final String mapToColumn = AnnotationReflector.getPropertyAnnotation(MapTo.class, rc, propName).value();
	    result.add(StringUtils.isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn);
	}
	return result;
    }

    private String getManyToOneProperty(final String propName, final String propColumn, final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	if (isOneToOne(entityType)) {
	    sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + propColumn
		    + "\" unique=\"true\" insert=\"false\" update=\"false\"/>\n");
	} else {
	    sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + propColumn + "\"/>\n");
	}

	return sb.toString();
    }

    private String getOneToOneProperty(final String propName, final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n");

	return sb.toString();
    }

    /**
     * Checks whether the given entity type is mapped to database.
     * @param entityType
     * @return
     */
    private boolean isPersisted(final Class entityType) {
	return AbstractEntity.class.isAssignableFrom(entityType) && AnnotationReflector.getAnnotation(MapEntityTo.class, entityType) != null;
    }

    private boolean isOneToOne(final Class entityType) {
	return AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(entityType));
    }

    private String getKeyMappingString(final Class entityType) {
	final String keyColumnOverride = AnnotationReflector.getAnnotation(MapEntityTo.class, entityType).keyColumn();
	if (!StringUtils.isEmpty(keyColumnOverride)) {
	    return getSimpleKeyWithColumn(keyColumnOverride);
	} else {
	    if (String.class.equals(AnnotationReflector.getKeyType(entityType))) {
		return  getSimpleKey();
	    } else if (isOneToOne(entityType)) {
		// Ignore if key type is DynamicEntityKey.class
		return getOneToOneProperty("key", AnnotationReflector.getKeyType(entityType));
	    } else {
		return null; //throw new RuntimeException("Can not generate mapping for key for type :" + entityType.getName());
	    }
	}
    }

    private String getClassMapping(final Class entityType) throws Exception {
	final StringBuffer sb = new StringBuffer();
	sb.append("<class name=\"" + entityType.getName() + "\" table=\"" + ddlGenerator.getTableClause(entityType) + "\">\n");
	sb.append(isOneToOne(entityType) ? getOneToOneEntityId() : getCommonEntityId());
	sb.append(getCommonEntityVersion());

	// Mapping key
	final String keyMapping = getKeyMappingString(entityType);
	if (keyMapping != null) {
	    sb.append(keyMapping);
	}

	if (AnnotationReflector.isAnnotationPresent(DescTitle.class, entityType)/* && !isOneToOne(entityType)*/) {
	    sb.append(getSimpleDesc());
	}

	final List<Field> properties = Finder.findProperties(entityType);
	for (final Field field : properties) {
	    if (!specialProps.contains(field.getName())) {
		final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, entityType, field.getName());
		if (mapTo != null) {
		    final String columnName = !StringUtils.isEmpty(mapTo.value()) ? mapTo.value(): field.getName().toUpperCase() + "_";


		    if (Collection.class.isAssignableFrom(field.getType())) {
			sb.append(getSet(field.getName(), columnName, PropertyTypeDeterminator.determinePropertyType(entityType, field.getName())));
		    } else if (isPersisted(field.getType())) {
			sb.append(getManyToOneProperty(field.getName(), columnName, field.getType()));
		    } else if (!StringUtils.isEmpty(mapTo.typeName())) {
			sb.append(getPlainProperty(field.getName(), columnName, mapTo.typeName()));
		    } else if (hibTypesInjector != null && !mapTo.userType().equals(Class.class)) {
			sb.append(getPlainProperty(field.getName(), columnName, hibTypesInjector.getInstance(mapTo.userType()).getClass()));
		    } else {
			sb.append(getPlainProperty(field.getName(), columnName, hibTypesDefaults.get(field.getType())));
		    }
		} else if (!Collection.class.isAssignableFrom(field.getType())) {
		    System.out.println(" " + entityType.getSimpleName() + " :: " + field.getName() + " has no MapTo");
		}
	    }
	}

	sb.append("</class>\n");
	return sb.toString();
    }

    public Type determinePropertyHibType(final Class<?> parentType, final String propName) throws Exception {
	if (EntityAggregates.class.equals(parentType) || IUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    return null;
	} else if (ICompositeUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    final CompositeUserType hibTypeInstance = (CompositeUserType) hibTypesInjector.getInstance(parentType);
	    return hibTypeInstance.getPropertyTypes()[Arrays.asList(hibTypeInstance.getPropertyNames()).indexOf(propName)];
	} else {
	    final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, parentType, propName);
	    if (!StringUtils.isEmpty(mapTo.typeName())) {
		return TypeFactory.basic(mapTo.typeName());
	    } else {
		final Class propType = PropertyTypeDeterminator.determinePropertyType(parentType, propName);
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
	if (EntityAggregates.class.equals(parentType) || IUserTypeInstantiate.class.isAssignableFrom(parentType) || ICompositeUserTypeInstantiate.class.isAssignableFrom(parentType)) {
	    return null;
	} else {
	    final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, parentType, propName);
	    if (StringUtils.isEmpty(mapTo.typeName())) {
		final Class hibType = hibTypesDefaults.get(PropertyTypeDeterminator.determinePropertyType(parentType, propName));
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
	    final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, parentType, propName);
	    if (Class.class.equals(mapTo.userType())) {
		final Class hibType = hibTypesDefaults.get(PropertyTypeDeterminator.determinePropertyType(parentType, propName));
		final Object hibTypeInstance = hibType == null ? null : hibType.newInstance();
		return hibTypeInstance instanceof ICompositeUserTypeInstantiate ? hibTypeInstance.getClass() : null;
	    } else {
		final Object hibTypeInstance = hibTypesInjector.getInstance(mapTo.userType());
		return hibTypeInstance instanceof ICompositeUserTypeInstantiate ? hibTypeInstance.getClass() : null;
	    }
	}
    }
}
