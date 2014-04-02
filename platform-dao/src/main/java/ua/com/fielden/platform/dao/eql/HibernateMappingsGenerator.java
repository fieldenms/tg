package ua.com.fielden.platform.dao.eql;

import static ua.com.fielden.platform.dao.DomainMetadata.specialProps;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.reflection.AnnotationReflector;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 * 
 * @author TG Team
 * 
 */
public class HibernateMappingsGenerator {

    public String generateMappings(final Collection<EntityMetadata> entityMetadatas) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
        sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
        sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
        sb.append("<hibernate-mapping default-access=\"field\">\n");

        for (final EntityMetadata entityMetadata : entityMetadatas) {
            try {
                if (entityMetadata.isPersisted()) {
                    sb.append(generateEntityClassMapping(entityMetadata));
                }
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Couldn't generate mapping for " + entityMetadata.getType().getName() + " due to: " + e.getMessage());
            }
            sb.append("\n");
        }
        sb.append("</hibernate-mapping>");

        final String result = sb.toString();
        System.out.println(result);
        return result;
    }

    private String generateEntityIdMapping(final String name, final PropertyColumn column, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<id name=\"" + name + "\" column=\"" + column.getName() + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t\t<generator class=\"hilo\">\n");
        sb.append("\t\t\t<param name=\"table\">UNIQUE_ID</param>\n");
        sb.append("\t\t\t<param name=\"column\">NEXT_VALUE</param>\n");
        sb.append("\t\t\t<param name=\"max_lo\">0</param>\n");
        sb.append("\t\t</generator>\n");
        sb.append("\t</id>\n");
        return sb.toString();
    }

    private String generateOneToOneEntityIdMapping(final String name, final PropertyColumn column, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<id name=\"" + name + "\" column=\"" + column.getName() + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t\t<generator class=\"foreign\">\n");
        sb.append("\t\t\t<param name=\"property\">key</param>\n");
        sb.append("\t\t</generator>\n");
        sb.append("\t</id>\n");

        return sb.toString();
    }

    private String generateEntityVersionMapping(final String name, final PropertyColumn column, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<version name=\"" + name + "\" type=\"" + hibTypeName + "\" access=\"field\" insert=\"false\">\n");
        sb.append("\t\t<column name=\"" + column.getName() + "\" default=\"0\" />\n");
        sb.append("\t</version>\n");
        return sb.toString();
    }

    private String generateManyToOnePropertyMapping(final String propName, final PropertyColumn propColumn, final Class entityType) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + propColumn.getName() + "\"");
        sb.append("/>\n");
        return sb.toString();
    }

    private String generateOneToOnePropertyMapping(final String propName, final Class entityType) {
        return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }

    private String generateUnionEntityPropertyMapping(final PropertyMetadata info) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<component name=\"" + info.getName() + "\" class=\"" + info.getJavaType().getName() + "\">\n");
        final List<Field> propsFields = AbstractUnionEntity.unionProperties(info.getJavaType());
        for (final Field subpropField : propsFields) {
            final MapTo mapTo = AnnotationReflector.getAnnotation(subpropField, MapTo.class);
            if (mapTo == null) {
                throw new IllegalStateException("Property [" + subpropField.getName() + "] in union entity type [" + info.getJavaType() + "] is not annotated  no MapTo ");
            }
            final String column = info.getColumn().getName() + "_" + (StringUtils.isEmpty(mapTo.value()) ? subpropField.getName() : mapTo.value());
            sb.append("\t\t<many-to-one name=\"" + subpropField.getName() + "\" class=\"" + subpropField.getType().getName() + "\" column = \"" + column.toUpperCase() + "\"/>\n");
        }
        sb.append("\t</component>\n");
        return sb.toString();
    }

    private String generatePlainPropertyMapping(final String propName, final List<PropertyColumn> propColumns, final String hibTypeName) {
        final String propNameClause = "\t<property name=\"" + propName + "\"";
        final String typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
        final String endClause = "/>\n";
        if (propColumns.size() == 1) {
            final PropertyColumn column = propColumns.get(0);
            final String columnClause = " column=\"" + column.getName() + "\"";
            final String lengthClause = column.getLength() == null ? "" : " length=\"" + column.getLength() + "\"";
            final String precisionClause = column.getPrecision() == null ? "" : " precision=\"" + column.getPrecision() + "\"";
            final String scaleClause = column.getScale() == null ? "" : " scale=\"" + column.getScale() + "\"";
            return propNameClause + columnClause + typeClause + lengthClause + precisionClause + scaleClause + endClause;
        } else {
            final StringBuffer sb = new StringBuffer();
            sb.append(propNameClause + typeClause + ">\n");
            for (final PropertyColumn column : propColumns) {
                sb.append("\t\t<column name=\"" + column.getName() + "\"" + endClause);
            }
            sb.append("\t</property>\n");
            return sb.toString();
        }
    }

    /**
     * Generates mapping for entity type.
     * 
     * @param entityType
     * @return
     * @throws Exception
     */
    private <ET extends AbstractEntity<?>> String generateEntityClassMapping(final EntityMetadata<ET> entityMetadata) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("<class name=\"" + entityMetadata.getType().getName() + "\" table=\"" + entityMetadata.getTable() + "\">\n");

        sb.append(generatePropertyMappingFromPropertyMetadata(entityMetadata.getProps().get(AbstractEntity.ID)));
        sb.append(generatePropertyMappingFromPropertyMetadata(entityMetadata.getProps().get(AbstractEntity.VERSION)));

        final PropertyMetadata keyProp = entityMetadata.getProps().get(AbstractEntity.KEY);
        if (keyProp.affectsMapping()) {
            sb.append(generatePropertyMappingFromPropertyMetadata(keyProp));
        }

        for (final PropertyMetadata ppi : entityMetadata.getProps().values()) {
            if (ppi.affectsMapping() && !specialProps.contains(ppi.getName())) {
                sb.append(generatePropertyMappingFromPropertyMetadata(ppi));
            }
        }
        sb.append("</class>\n");
        return sb.toString();
    }

    /**
     * Generates mapping string for common property based on it persistence info.
     * 
     * @param propMetadata
     * @return
     * @throws Exception
     */
    private String generatePropertyMappingFromPropertyMetadata(final PropertyMetadata propMetadata) throws Exception {
        switch (propMetadata.getType()) {
        case UNION_ENTITY_HEADER:
            return generateUnionEntityPropertyMapping(propMetadata);
        case ENTITY_AS_KEY:
            return generateOneToOnePropertyMapping(propMetadata.getName(), propMetadata.getJavaType());
        case ENTITY:
        case ENTITY_MEMBER_OF_COMPOSITE_KEY:
            return generateManyToOnePropertyMapping(propMetadata.getName(), propMetadata.getColumn(), propMetadata.getJavaType());
        case VERSION:
            return generateEntityVersionMapping(propMetadata.getName(), propMetadata.getColumn(), propMetadata.getTypeString());
        case ID:
            return generateEntityIdMapping(propMetadata.getName(), propMetadata.getColumn(), propMetadata.getTypeString());
        case ONE2ONE_ID:
            return generateOneToOneEntityIdMapping(propMetadata.getName(), propMetadata.getColumn(), propMetadata.getTypeString());
        default:
            return generatePlainPropertyMapping(propMetadata.getName(), propMetadata.getColumns(), propMetadata.getTypeString());
        }
    }
}