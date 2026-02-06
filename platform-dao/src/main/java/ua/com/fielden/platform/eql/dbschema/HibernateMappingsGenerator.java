package ua.com.fielden.platform.eql.dbschema;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.eql.meta.PropColumn;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.types.either.Either;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

/// Generates hibernate mappings for persistent domain entity types.
///
public class HibernateMappingsGenerator {
    private static final Logger LOGGER = getLogger(HibernateMappingsGenerator.class);

    public static final String ID_SEQUENCE_NAME = "TG_ENTITY_ID_SEQ";

    private static final Set<String> SPECIAL_PROPS = Set.of(ID, KEY, VERSION);

    public static final String ERR_UNEXPECTED_PROP_NATURE = "Expected property [%s] to have nature [%s].";

    private final IDomainMetadata domainMetadata;
    private final IDomainMetadataUtils domainMetadataUtils;
    private final EqlTables eqlTables;
    private final IDbVersionProvider dbVersionProvider;
    private final PropertyMetadataUtils pmUtils;
    private final PropertyInliner propertyInliner;

    @Inject
    public HibernateMappingsGenerator(
            final IDomainMetadata domainMetadata,
            final IDomainMetadataUtils domainMetadataUtils,
            final IDbVersionProvider dbVersionProvider,
            final EqlTables eqlTables,
            final PropertyInliner propertyInliner)
    {
        this.eqlTables = eqlTables;
        this.domainMetadata = domainMetadata;
        this.domainMetadataUtils = domainMetadataUtils;
        this.pmUtils = domainMetadata.propertyMetadataUtils();
        this.dbVersionProvider = dbVersionProvider;
        this.propertyInliner = propertyInliner;
    }

    public String generateMappings() {
        final var sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
        sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
        sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
        sb.append("<hibernate-mapping default-access=\"field\">\n");

        domainMetadataUtils.registeredEntities()
                .distinct()
                .filter(EntityMetadata::isPersistent)
                .sorted(comparing(em -> em.javaType().getSimpleName())) // sort for testing purposes
                .forEach(em -> {
                    try {
                        final var tableName = eqlTables.getTableForEntityType(em.javaType()).name();
                        sb.append(generateEntityClassMapping(domainMetadata, em, tableName, dbVersionProvider.dbVersion()));
                    } catch (final Exception ex) {
                        LOGGER.error(ex);
                        throw new EqlMetadataGenerationException("Could not generate mapping for " + em, ex);
                    }
                    sb.append("\n");
                });
        sb.append("</hibernate-mapping>");

        return sb.toString();
    }

    private static String generateEntityIdMapping(final String name, final String columnName, final String hibTypeName) {
        final var sb = new StringBuilder();
        sb.append("\t<id name=\"").append(name).append("\" column=\"").append(columnName).append("\" type=\"").append(hibTypeName).append("\" access=\"property\">\n");
        sb.append("\t</id>\n");
        return sb.toString();
    }

    private static String generateOneToOneEntityIdMapping(final String name, final String columnName, final String hibTypeName) {
        final var sb = new StringBuilder();
        sb.append("\t<id name=\"").append(name).append("\" column=\"").append(columnName).append("\" type=\"").append(hibTypeName).append("\" access=\"property\">\n");
        sb.append("\t\t<generator class=\"foreign\">\n");
        sb.append("\t\t\t<param name=\"property\">key</param>\n");
        sb.append("\t\t</generator>\n");
        sb.append("\t</id>\n");

        return sb.toString();
    }

    private static String generateEntityVersionMapping(final String name, final String columnName, final String hibTypeName) {
        final var sb = new StringBuilder();
        // insert: whether or not to include the version column in SQL insert statements.
        //         Defaults to true, but you can set it to false if the database column is defined with a default value of 0.
        sb.append("\t<version name=\"").append(name).append("\" type=\"").append(hibTypeName).append("\" access=\"field\" insert=\"false\">\n");
        sb.append("\t\t<column name=\"").append(columnName).append("\" default=\"0\" />\n");
        sb.append("\t</version>\n");
        return sb.toString();
    }

    private static String generateManyToOnePropertyMapping(final String propName, final String columnName, final Class<?> entityType) {
        final var sb = new StringBuilder();
        sb.append("\t<many-to-one name=\"").append(propName).append("\" class=\"").append(entityType.getName()).append("\" column=\"").append(columnName).append("\"");
        sb.append("/>\n");
        return sb.toString();
    }

    private static String generateOneToOnePropertyMapping(final String propName, final Class<?> entityType) {
        return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }

    private static String generateUnionEntityPropertyMapping(final PropertyMetadata pm, final PropertyMetadataUtils pmUtils) {
        final var entityType = pm.type().javaType();

        final var sb = new StringBuilder();
        sb.append("\t<component name=\"").append(pm.name()).append("\" class=\"").append(entityType.getName()).append("\">\n");

        pmUtils.subProperties(pm).stream()
                .flatMap(spm -> spm.asPersistent().stream())
                .map(spm -> {
                    final var spType = spm.type().javaType();
                    return "\t\t<many-to-one name=\"" + spm.name() + "\" class=\"" + spType.getName() + "\" column = \"" + spm.data().column().name.toUpperCase() + "\"/>\n";
                })
                .forEach(sb::append);

        sb.append("\t</component>\n");
        return sb.toString();
    }

    /// @param column  either a single column or multiple column names
    ///
    private static String generatePlainPropertyMapping(
            final String propName,
            final Either<PropColumn, List<String>> column,
            final String hibTypeName)
    {
        final var propNameClause = "\t<property name=\"" + propName + "\"";
        final var typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
        final var endClause = "/>\n";
        return column.fold(
                singleColumn -> {
                    final var columnClause = " column=\"" + singleColumn.name + "\"";
                    final var lengthClause = singleColumn.length == null ? "" : " length=\"" + singleColumn.length + "\"";
                    final var precisionClause = singleColumn.precision == null ? "" : " precision=\"" + singleColumn.precision + "\"";
                    final var scaleClause = singleColumn.scale == null ? "" : " scale=\"" + singleColumn.scale + "\"";
                    return propNameClause + columnClause + typeClause + lengthClause + precisionClause + scaleClause + endClause;
                },
                multipleColumns -> {
                    final var sb = new StringBuilder();
                    sb.append(propNameClause).append(typeClause).append(">\n");
                    for (final String name : multipleColumns) {
                        // TODO: The following condition is responsible for identifying and mapping RichText.searchText as a write-only component.
                        //       Ultimately, condition `name.endsWith("_SEARCHTEXT")` should be changed in favor of explicit support for write-only components/properties more generically.
                        //       The write-only aspect is implemented by specifying attribute `read="NULL"`, which results in returning NULL for searchText instead of the actual value upon data retrieval.
                        //       The Hibernate Mapping DTD (https://hibernate.org/dtd/hibernate-mapping-3.0.dtd) defaults attribute `read` to a column name, but it can be any valid SQL expression.
                        //       Assigning this attribute to `NULL` results in `NULL` being retrieved from a database, saving some valuable bandwidth.
                        sb.append("\t\t<column name=\"").append(name).append("\"");
                        if (name.endsWith("_SEARCHTEXT")) {
                            sb.append(" read=\"NULL\"");
                        }
                        sb.append(endClause);
                    }
                    sb.append("\t</property>\n");
                    return sb.toString();
                });
    }

    /// Generates mapping for an entity type.
    ///
    private String generateEntityClassMapping(
            final IDomainMetadata domainMetadata,
            final EntityMetadata em,
            final String tableName,
            final DbVersion dbVersion)
    {
        final Class<? extends AbstractEntity<?>> entityType= em.javaType();
        final var sb = new StringBuilder();
        sb.append("<class name=\"").append(entityType.getName()).append("\" table=\"").append(tableName).append("\">\n");

        sb.append(em.propertyOpt(ID).flatMap(PropertyMetadata::asPersistent).map(pm -> {
            if (isOneToOne(entityType)) {
                return generateOneToOneEntityIdMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
            } else {
                return generateEntityIdMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
            }
        }).orElseThrow(() -> unexpectedPropNature("%s.%s".formatted(entityType.getSimpleName(), ID), PropertyNature.PERSISTENT)));

        sb.append(em.propertyOpt(VERSION).flatMap(PropertyMetadata::asPersistent).map(pm -> {
            return generateEntityVersionMapping(pm.name(), pm.data().column().name, pm.hibType().getClass().getName());
        }).orElseThrow(() -> unexpectedPropNature("%s.%s".formatted(entityType.getSimpleName(), VERSION), PropertyNature.PERSISTENT)));

        em.property(KEY).asPersistent().ifPresent(pm -> sb.append(generatePropertyMappingFromPropertyMetadata(domainMetadata, pm)));

        em.properties().stream()
                // sort for testing purposes
                .sorted(comparing(PropertyMetadata::name))
                .filter(pm -> !SPECIAL_PROPS.contains(pm.name()))
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .forEach(pm -> sb.append(generatePropertyMappingFromPropertyMetadata(domainMetadata, pm)));

        sb.append("</class>\n");
        return sb.toString();
    }

    /// Generates mapping string for a common property based on it persistence info.
    ///
    private String generatePropertyMappingFromPropertyMetadata(
            final IDomainMetadata domainMetadata,
            final PropertyMetadata.Persistent prop)
    {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        if (pmUtils.isPropEntityType(prop, EntityMetadata::isUnion)) {
            return generateUnionEntityPropertyMapping(prop, pmUtils);
        }
        return propertyInliner.inline(prop)
                .map(props -> props.stream().map(p -> p.data().column()).toList())
                .map(columns -> {
                    final Either<PropColumn, List<String>> column = columns.size() == 1
                            ? left(columns.getFirst())
                            : right(columns.stream().map(c -> c.name).toList());
                    return generatePlainPropertyMapping(prop.name(), column, prop.hibType().getClass().getName());
                })
                .orElseGet(() -> {
                    if (pmUtils.isPropEntityType(prop, EntityMetadata::isPersistent)) {
                        final var et = prop.type().asEntity().orElseThrow();
                        if (KEY.equals(prop.name())) {
                            return generateOneToOnePropertyMapping(prop.name(), et.javaType());
                        } else {
                            return generateManyToOnePropertyMapping(prop.name(), prop.data().column().name, et.javaType());
                        }
                    }
                    else {
                        return generatePlainPropertyMapping(prop.name(), left(prop.data().column()), prop.hibType().getClass().getName());
                    }
                });
    }

    private static DbSchemaException unexpectedPropNature(final String prop, final PropertyNature expectedNature) {
        return new DbSchemaException(ERR_UNEXPECTED_PROP_NATURE.formatted(prop, expectedNature));
    }

}
