package ua.com.fielden.platform.eql.dbschema;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.PropColumn;
import ua.com.fielden.platform.meta.*;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.SPECIAL_PROPS;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class HibernateMappingsGenerator {
    private static final Logger LOGGER = getLogger(HibernateMappingsGenerator.class);

    public static final String ID_SEQUENCE_NAME = "TG_ENTITY_ID_SEQ";

    private final IDomainMetadata domainMetadata;
    private final DbVersion dbVersion;
    private final PropertyMetadataUtils pmUtils;

    public HibernateMappingsGenerator(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
        this.pmUtils = domainMetadata.propertyMetadataUtils();
        this.dbVersion = domainMetadata.dbVersion();
    }

    public String generateMappings() {
        final StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
        sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
        sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
        sb.append("<hibernate-mapping default-access=\"field\">\n");

        domainMetadata.allTypes(EntityMetadata.class).stream().distinct()
                .filter(em -> em.nature().isPersistent())
                .forEach(em -> {
                    try {
                        String tableName = domainMetadata.getTableForEntityType(em.javaType()).name();
                        sb.append(generateEntityClassMapping(domainMetadata, em, tableName, dbVersion));
                    } catch (final Exception e) {
                        LOGGER.error(e);
                        throw new EqlMetadataGenerationException("Couldn't generate mapping for " + em, e);
                    }
                    sb.append("\n");
                });
        sb.append("</hibernate-mapping>");

        final String result = sb.toString();
        LOGGER.debug("\n\n" + result + "\n\n");
        return result;
    }

    private String generateEntityIdMapping(final String name, final String columnName, final String hibTypeName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t<id name=\"" + name + "\" column=\"" + columnName + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t</id>\n");
        return sb.toString();
    }

    private String generateOneToOneEntityIdMapping(final String name, final String columnName, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<id name=\"" + name + "\" column=\"" + columnName + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
        sb.append("\t\t<generator class=\"foreign\">\n");
        sb.append("\t\t\t<param name=\"property\">key</param>\n");
        sb.append("\t\t</generator>\n");
        sb.append("\t</id>\n");

        return sb.toString();
    }

    private String generateEntityVersionMapping(final String name, final String columnName, final String hibTypeName) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<version name=\"" + name + "\" type=\"" + hibTypeName + "\" access=\"field\" insert=\"false\">\n");
        sb.append("\t\t<column name=\"" + columnName + "\" default=\"0\" />\n");
        sb.append("\t</version>\n");
        return sb.toString();
    }

    private String generateManyToOnePropertyMapping(final String propName, final String columnName, final Class entityType) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + columnName + "\"");
        sb.append("/>\n");
        return sb.toString();
    }

    private String generateOneToOnePropertyMapping(final String propName, final Class entityType) {
        return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }

    private String generateUnionEntityPropertyMapping(final PropertyMetadata pm) {
        final var entityType = (Class<?>) pm.type().javaType();

        final StringBuffer sb = new StringBuffer();
        sb.append("\t<component name=\"" + pm.name() + "\" class=\"" + entityType.getName() + "\">\n");

        pmUtils.subProperties(pm).stream()
                .flatMap(spm -> spm.asPersistent().stream())
                .map(spm -> {
                    final var spType = (Class<?>) spm.type().javaType();
                    return "\t\t<many-to-one name=\"" + spm.name() + "\" class=\"" + spType.getName() + "\" column = \"" + spm.data().column().name.toUpperCase() + "\"/>\n";
                })
                .forEach(sb::append);

        sb.append("\t</component>\n");
        return sb.toString();
    }

    private String generatePlainPropertyMapping(final String propName, final PropColumn singleColumn, final List<String> multipleColumns, final String hibTypeName) {
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
     * Generates mapping for an entity type.
     */
    private String generateEntityClassMapping(final IDomainMetadata domainMetadata, final EntityMetadata em,
                                              final String tableName, final DbVersion dbVersion)
    {
        final Class<? extends AbstractEntity<?>> entityType= em.javaType();
        final StringBuffer sb = new StringBuffer();
        sb.append("<class name=\"" + entityType.getName() + "\" table=\"" + tableName + "\">\n");

        sb.append(em.property(ID).flatMap(PropertyMetadata::asPersistent).map(pm -> {
            if (isOneToOne(entityType)) {
                return generateOneToOneEntityIdMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
            } else {
                return generateEntityIdMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
            }
        }).orElseThrow(() -> unexpectedPropNature("%s.%s".formatted(entityType.getSimpleName(), ID), PropertyNature.PERSISTENT)));

        sb.append(em.property(VERSION).flatMap(PropertyMetadata::asPersistent).map(pm -> {
            return generateEntityVersionMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
        }).orElseThrow(() -> unexpectedPropNature("%s.%s".formatted(entityType.getSimpleName(), VERSION), PropertyNature.PERSISTENT)));

        em.property(KEY).orElseThrow(() -> new InvalidArgumentException("Missing property [%s] in [%s].".formatted(KEY, em)))
                .asPersistent()
                .ifPresent(pm -> sb.append(generatePropertyMappingFromPropertyMetadata(domainMetadata, pm)));

        em.properties().stream()
                .filter(pm -> !pm.nature().isCalculated() && !SPECIAL_PROPS.contains(pm.name())
                              && (pm.nature().isPersistent() || anySubPropMatches(pm, spm -> spm.nature().isPersistent())))
                .forEach(pm -> sb.append(generatePropertyMappingFromPropertyMetadata(domainMetadata, pm)));

        sb.append("</class>\n");
        return sb.toString();
    }

    private boolean anySubPropMatches(final PropertyMetadata pm, final Predicate<? super PropertyMetadata> predicate) {
        return pmUtils.subProperties(pm).stream().anyMatch(predicate);
    }

    /**
     * Generates mapping string for common property based on it persistence info.
     *
     * @param pm
     * @return
     * @throws Exception
     */
    private String generatePropertyMappingFromPropertyMetadata(final IDomainMetadata domainMetadata, final PropertyMetadata pm) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        // TODO replace by a PropertyTypeVisitor
        if (pmUtils.isPropEntityType(pm, em -> em.nature().isUnion())) {
            return generateUnionEntityPropertyMapping(pm);
        }
        else if (pmUtils.isPropEntityType(pm, em -> em.nature().isPersistent())) {
            final var et = (PropertyTypeMetadata.Entity) pm.type();
            if (KEY.equals(pm.name())) {
                return generateOneToOnePropertyMapping(pm.name(), et.javaType());
            }
            else {
                return pm.asPersistent()
                        .map(ppm -> generateManyToOnePropertyMapping(ppm.name(), ppm.data().column().name, et.javaType()))
                        .orElseThrow(() -> unexpectedPropNature(pm, PropertyNature.PERSISTENT));
            }
        }
        else {
            final List<PropColumn> subColumns = pmUtils.subProperties(pm).stream()
                    .flatMap(spm -> spm.asPersistent().stream())
                    .map(spm -> spm.data().column())
                    .toList();

            final PropColumn singleColumn = subColumns.size() == 1
                    ? subColumns.getFirst()
                    : pm.asPersistent().map(ppm -> ppm.data().column()).orElse(null);
            return generatePlainPropertyMapping(pm.name(), singleColumn,
                                                singleColumn == null ? subColumns.stream().map(c -> c.name).toList() : emptyList(),
                                                pm.hibType().getClass().getName());
        }
    }

    private static IllegalArgumentException unexpectedPropNature(Object prop, Object expectedNature) {
        return new IllegalArgumentException("Expected property [%s] to have nature [%s].".formatted(prop, expectedNature));
    }

}
