package ua.com.fielden.platform.eql.dbschema;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// Covers the invariant that union expressions used in DDL generation match those produced by EQL.
/// This property enables an SQL optimiser to match an expression produced by EQL with a corresponding index.
///
public class UnionExpressionDdlEqlMatchTest extends AbstractDaoTestCase {

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
    private final DbVersion dbVersion = getInstance(IDbVersionProvider.class).dbVersion();
    private final HibernateTypeMappings typeMappings = getInstance(HibernateTypeMappings.class);
    private final EqlQueryTransformer eqlTransformer = getInstance(EqlQueryTransformer.class);

    @WithDbVersion(DbVersion.MSSQL)
    @Test
    public void expression_for_computed_union_column_is_equal_to_EQL_expression_for_union_typed_property_ID() {
        assertTrue(domainMetadata.propertyMetadataUtils().isPropEntityType(domainMetadata.forProperty(TgBogie.class, TgBogie.LOCATION).type(), EntityMetadata::isUnion));

        final var dialect = HibernateHelpers.getDialect(dbVersion);
        final var ddl = new TableDdl(new ColumnDefinitionExtractor(typeMappings, dialect), TgBogie.class);
        final var query = select(TgBogie.class).yield().prop(TgBogie.LOCATION + "." + ID).modelAsEntity(TgBogie.class);
        final var sql = eqlTransformer.getModelResult(new QueryProcessingModel<>(query, null, null, Map.of(), false), Optional.empty()).sql();
        assertThat(sql).isEqualTo(
                """
                SELECT
                CASE WHEN T_1.LOCATION__WAGONSLOT IS NOT NULL THEN T_1.LOCATION__WAGONSLOT WHEN T_1.LOCATION__WORKSHOP IS NOT NULL THEN T_1.LOCATION__WORKSHOP END AS C_2
                FROM
                TGBOGIE_ AS T_1""");
        assertThat(ddl.getColumnDefinition(TgBogie.LOCATION).maybeExpression)
                .contains("CASE WHEN LOCATION__WAGONSLOT IS NOT NULL THEN LOCATION__WAGONSLOT WHEN LOCATION__WORKSHOP IS NOT NULL THEN LOCATION__WORKSHOP END");
    }

    @WithDbVersion(DbVersion.POSTGRESQL)
    @Test
    public void expression_for_union_index_is_equal_to_EQL_expression_for_union_typed_property_ID() {
        assertTrue(domainMetadata.propertyMetadataUtils().isPropEntityType(domainMetadata.forProperty(TgBogie.class, TgBogie.LOCATION).type(), EntityMetadata::isUnion));

        final var dialect = HibernateHelpers.getDialect(dbVersion);
        final var ddl = new TableDdl(new ColumnDefinitionExtractor(typeMappings, dialect), TgBogie.class);
        final var query = select(TgBogie.class).yield().prop(TgBogie.LOCATION + "." + ID).modelAsEntity(TgBogie.class);
        final var sql = eqlTransformer.getModelResult(new QueryProcessingModel<>(query, null, null, Map.of(), false), Optional.empty()).sql();
        assertThat(sql).isEqualTo(
                """
                SELECT
                CASE WHEN T_1.LOCATION__WAGONSLOT IS NOT NULL THEN T_1.LOCATION__WAGONSLOT WHEN T_1.LOCATION__WORKSHOP IS NOT NULL THEN T_1.LOCATION__WORKSHOP END AS C_2
                FROM
                TGBOGIE_ AS T_1""");
        assertThat(ddl.createIndicesSchema(dialect))
                .contains("CREATE INDEX I_TGBOGIE__LOCATION_ ON TGBOGIE_" +
                          "((CASE WHEN LOCATION__WAGONSLOT IS NOT NULL THEN LOCATION__WAGONSLOT WHEN LOCATION__WORKSHOP IS NOT NULL THEN LOCATION__WORKSHOP END) ASC)");
    }

}
