package ua.com.fielden.platform.dao2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.AbstractEntity;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class HibernateMappingsGenerator {
    private static List<String> specialProps = Arrays.asList(new String[] { "id", "key", "version" });
    private final Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> hibTypeInfosMap;

    public HibernateMappingsGenerator(final Map<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> hibTypeInfosMap) {
	this.hibTypeInfosMap = hibTypeInfosMap;
    }

    public String generateMappings() {
	final StringBuffer sb = new StringBuffer();
	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
	sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
	sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
	sb.append("<hibernate-mapping default-access=\"field\">\n");

	for (final Map.Entry<Class<? extends AbstractEntity<?>>, EntityPersistenceMetadata> entityTypeEntry : hibTypeInfosMap.entrySet()) {
	    try {
		sb.append(getClassMapping(entityTypeEntry.getValue()));
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new RuntimeException("Couldn't generate mapping for " + entityTypeEntry.getKey().getName() + " due to: " + e.getMessage());
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

    private String getPropMappingString(final String propName, final List<String> propColumns, final String hibTypeName, final Long length, final Long precision, final Long scale) {
	final String propNameClause = "\t<property name=\"" + propName + "\"";
	final String typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
	final String lengthClause = length == null ? "" : " length=\"" + length + "\"";
	final String precisionClause = precision == null ? "" : " precision=\"" + precision + "\"";
	final String scaleClause = scale == null ? "" : " scale=\"" + scale + "\"";
	final String endClause = "/>\n";
	if (propColumns.size() == 1) {
	    final String columnClause = " column=\"" + propColumns.get(0) + "\"";
	    return propNameClause + columnClause + typeClause + lengthClause + precisionClause + scaleClause + endClause;
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
    private String getClassMapping(final EntityPersistenceMetadata map) throws Exception {
	final StringBuffer sb = new StringBuffer();
	sb.append("<class name=\"" + map.getType().getName() + "\" table=\"" + map.getTable() + "\">\n");

	sb.append(getCommonPropMappingString(map.getProps().get("id")));
	sb.append(getCommonPropMappingString(map.getProps().get("version")));
	if (map.getProps().get("key") != null) {
	    sb.append(getCommonPropMappingString(map.getProps().get("key")));
	}

	for (final PropertyPersistenceInfo ppi : map.getProps().values()) {
	    if (!ppi.getType().equals(PropertyPersistenceType.COMPOSITE_DETAILS) && !specialProps.contains(ppi.getName())) {
		sb.append(getCommonPropMappingString(ppi));
	    }
	}
	sb.append("</class>\n");
	return sb.toString();
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
	    return getPropMappingString(info.getName(), info.getColumns(), info.getTypeString(), info.getLength(), info.getPrecision(), info.getScale());
	}
    }
}