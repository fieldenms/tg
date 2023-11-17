package ua.com.fielden.platform.eql.dbschema;

import static java.util.Collections.emptyList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.SPECIAL_PROPS;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.EqlEntityMetadata;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata;
import ua.com.fielden.platform.eql.meta.PropColumn;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class HibernateMappingsGenerator {
    private static final Logger LOGGER = getLogger(HibernateMappingsGenerator.class);

    public static final String ID_SEQUENCE_NAME = "TG_ENTITY_ID_SEQ";

    public static String generateMappings(final EqlDomainMetadata domainMetadata) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
        sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
        sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
        sb.append("<hibernate-mapping default-access=\"field\">\n");

        for (final EqlEntityMetadata<?> entry : domainMetadata.entityPropsMetadata().values()) {
            if (entry.typeInfo.category == PERSISTENT) {
                try {
                    sb.append(generateEntityClassMapping(entry.entityType, domainMetadata.entityMetadataHolder.getTableForEntityType(entry.entityType).name(), sortPropsMetadata(entry.props()), domainMetadata.dbVersion));
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new EqlMetadataGenerationException("Couldn't generate mapping for " + entry.entityType.getName() + " due to: " + e.getMessage());
                }
                sb.append("\n");
            }
        }
        sb.append("</hibernate-mapping>");

        final String result = sb.toString();
        LOGGER.debug("\n\n" + result + "\n\n");
        return result;
    }

    private static final Collection<EqlPropertyMetadata> sortPropsMetadata(final List<EqlPropertyMetadata> propMds) {
        final SortedMap<String, EqlPropertyMetadata> sorted = new TreeMap<>();
        for (final EqlPropertyMetadata propMd : propMds) {
            sorted.put(propMd.name, propMd);
        }
        return sorted.values();
    }

    private static String generateEntityIdMapping(final String name, final String columnName, final String hibTypeName, final DbVersion dbVersion) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t<id name=\"" + name + "\" column=\"" + columnName + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t</id>\n");
        return sb.toString();
    }

    private static String generateOneToOneEntityIdMapping(final String name, final String columnName, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<id name=\"" + name + "\" column=\"" + columnName + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t\t<generator class=\"foreign\">\n");
        sb.append("\t\t\t<param name=\"property\">key</param>\n");
        sb.append("\t\t</generator>\n");
        sb.append("\t</id>\n");

        return sb.toString();
    }

    private static String generateEntityVersionMapping(final String name, final String columnName, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<version name=\"" + name + "\" type=\"" + hibTypeName + "\" access=\"field\" insert=\"false\">\n");
        sb.append("\t\t<column name=\"" + columnName + "\" default=\"0\" />\n");
        sb.append("\t</version>\n");
        return sb.toString();
    }

    private static String generateManyToOnePropertyMapping(final String propName, final String columnName, final Class entityType) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + columnName + "\"");
        sb.append("/>\n");
        return sb.toString();
    }

    private static String generateOneToOnePropertyMapping(final String propName, final Class entityType) {
        return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }

    private static String generateUnionEntityPropertyMapping(final EqlPropertyMetadata info) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<component name=\"" + info.name + "\" class=\"" + info.javaType.getName() + "\">\n");
        for (final EqlPropertyMetadata subpropField : info.subitems) {
            if (subpropField.column != null) {
                sb.append("\t\t<many-to-one name=\"" + subpropField.name + "\" class=\"" + subpropField.javaType.getName() + "\" column = \"" + subpropField.column.name.toUpperCase() + "\"/>\n");
            }
        }
        sb.append("\t</component>\n");
        return sb.toString();
    }

    private static String generatePlainPropertyMapping(final String propName, final PropColumn singleColumn, final List<String> multipleColumns, final String hibTypeName) {
        final String propNameClause = "\t<property name=\"" + propName + "\"";
        final String typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
        final String endClause = "/>\n";
        if (multipleColumns.isEmpty()) {
            final String columnClause = " column=\"" + singleColumn.name + "\"";
            final String lengthClause = singleColumn.length == null ? "" : " length=\"" + singleColumn.length + "\"";
            final String precisionClause = singleColumn.precision == null ? "" : " precision=\"" + singleColumn.precision + "\"";
            final String scaleClause = singleColumn.scale == null ? "" : " scale=\"" + singleColumn.scale + "\"";
            return propNameClause + columnClause + typeClause + lengthClause + precisionClause + scaleClause + endClause;
        } else {
            final StringBuffer sb = new StringBuffer();
            sb.append(propNameClause + typeClause + ">\n");
            for (final String column : multipleColumns) {
                sb.append("\t\t<column name=\"" + column + "\"" + endClause);
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
    private static <ET extends AbstractEntity<?>> String generateEntityClassMapping(final Class<? extends AbstractEntity<?>> type, final String tableName, final Collection<EqlPropertyMetadata> propsMetadata, final DbVersion dbVersion) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("<class name=\"" + type.getName() + "\" table=\"" + tableName + "\">\n");

        final EqlPropertyMetadata id = propsMetadata.stream().filter(e -> ID.equals(e.name)).findAny().get();
        if (isOneToOne(type)) {
            sb.append(generateOneToOneEntityIdMapping(id.name, id.column.name, id.hibType.getClass().getName()));
        } else {
            sb.append(generateEntityIdMapping(id.name, id.column.name, id.hibType.getClass().getName(), dbVersion));
        }
        final EqlPropertyMetadata version = propsMetadata.stream().filter(e -> VERSION.equals(e.name)).findAny().get();
        sb.append(generateEntityVersionMapping(version.name, version.column.name, version.hibType.getClass().getName()));

        final EqlPropertyMetadata keyProp = propsMetadata.stream().filter(e -> KEY.equals(e.name)).findAny().get();
        if (keyProp.column != null) {
            sb.append(generatePropertyMappingFromPropertyMetadata(keyProp));
        }

        for (final EqlPropertyMetadata ppi : propsMetadata) {
            if (ppi.expressionModel == null && !SPECIAL_PROPS.contains(ppi.name) && (ppi.column != null || ppi.subitems.stream().anyMatch(e -> e.column != null))) {
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
    private static String generatePropertyMappingFromPropertyMetadata(final EqlPropertyMetadata propMetadata) throws Exception {
        if (isUnionEntityType(propMetadata.javaType)) {
            return generateUnionEntityPropertyMapping(propMetadata);
        } else if (isPersistedEntityType(propMetadata.javaType)) {
            if (KEY.equals(propMetadata.name)) {
                return generateOneToOnePropertyMapping(propMetadata.name, propMetadata.javaType);
            } else {
                return generateManyToOnePropertyMapping(propMetadata.name, propMetadata.column.name, propMetadata.javaType);
            }
        } else {
            final List<String> columns = new ArrayList<>();
            for (final EqlPropertyMetadata subitem : propMetadata.subitems) {
                if (subitem.expressionModel == null) {
                    columns.add(subitem.column.name);
                }
            }
            final PropColumn singleColumn = propMetadata.subitems.size() == 1 ? propMetadata.subitems.get(0).column : propMetadata.column;
            return generatePlainPropertyMapping(propMetadata.name, singleColumn, singleColumn == null ? columns : emptyList(), propMetadata.hibType.getClass().getName());
        }
    }
}
