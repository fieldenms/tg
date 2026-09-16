package ua.com.fielden.platform.eql.dbschema;

import org.hibernate.dialect.Dialect;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.test_entities.*;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.entity.query.DbVersion.*;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;

/// Tests for DDL generation by [TableDdl].
///
/// Statements for the primary key, indices and foreign keys are asserted verbatim, as they do not depend on the RDBMS dialect.
/// Statements for tables do depend on the dialect, so they are asserted for a specific [DbVersion].
///
public class TableDdlTest {

    /// Bundles a [TableDdl] with the dialect it was built for, as every DDL-generating method requires both.
    ///
    private record Ddl (Dialect dialect, TableDdl tableDdl) {

        static Ddl of(final DbVersion dbVersion, final Class<? extends AbstractEntity<?>> entityType) {
            final Dialect dialect = HibernateHelpers.getDialect(dbVersion);
            final var typeMappings = new PlatformHibernateTypeMappings.Provider(constantDbVersion(dbVersion)).get();
            return new Ddl(dialect, new TableDdl(new ColumnDefinitionExtractor(typeMappings, dialect), entityType));
        }

        String table() {
            return tableDdl.createTableSchema(dialect);
        }

        String pk() {
            return tableDdl.createPkSchema(dialect);
        }

        List<String> indexes() {
            return tableDdl.createIndicesSchema(dialect);
        }

        List<String> fks() {
            return tableDdl.createFkSchema(dialect);
        }

    }

    // ------------------------------------------------------------------------------------------------------------
    // Table names
    // ------------------------------------------------------------------------------------------------------------

    @Test
    public void table_name_defaults_to_the_uppercased_entity_type_name_with_a_trailing_underscore() {
        assertThat(TableDdl.tableName(Entity_Simple.class)).isEqualTo("ENTITY_SIMPLE_");
        assertThat(Ddl.of(H2, Entity_Simple.class).tableDdl().getTableName()).isEqualTo("ENTITY_SIMPLE_");
    }

    @Test
    public void table_name_is_taken_from_MapEntityTo_when_it_specifies_one() {
        assertThat(TableDdl.tableName(Entity_WithRefs.class)).isEqualTo("REFS_TABLE");
        assertThat(Ddl.of(H2, Entity_WithRefs.class).tableDdl().getTableName()).isEqualTo("REFS_TABLE");
    }

    // ------------------------------------------------------------------------------------------------------------
    // CREATE TABLE
    // ------------------------------------------------------------------------------------------------------------

    /// Covers, in one statement: the leading `_ID`, `KEY_` and `_VERSION` columns; the `NOT NULL` constraint of a
    /// required property; the absence of one for an optional property; a column name supplied by `@MapTo`;
    /// and a `DEFAULT` clause supplied by `@MapTo(defaultValue = ...)`.
    ///
    @Test
    public void table_schema_maps_id_key_and_version_first_and_then_each_property_in_declaration_order() {
        assertThat(Ddl.of(POSTGRESQL, Entity_Simple.class).table())
                .isEqualTo("CREATE TABLE ENTITY_SIMPLE_ ( " +
                           "_ID int8 NOT NULL, " +
                           "KEY_ varchar(255) NOT NULL, " +
                           "_VERSION int8 NOT NULL DEFAULT 0, " +
                           "REQUIREDSTR_ varchar(255) NOT NULL, " +
                           "OPTIONALINT_ int4, " +
                           "ACTIVE_ char(1) NOT NULL, " +
                           "CUSTOM_COLUMN varchar(255), " +
                           "WITHDEFAULTVALUE_ int4 DEFAULT 0 );");
    }

    @Test
    public void column_types_are_dialect_specific() {
        assertThat(Ddl.of(MSSQL, Entity_Simple.class).table())
                .isEqualTo("CREATE TABLE ENTITY_SIMPLE_ ( " +
                           "_ID bigint NOT NULL, " +
                           "KEY_ varchar(255) NOT NULL, " +
                           "_VERSION bigint NOT NULL DEFAULT 0, " +
                           "REQUIREDSTR_ varchar(255) NOT NULL, " +
                           "OPTIONALINT_ int, " +
                           "ACTIVE_ char(1) NOT NULL, " +
                           "CUSTOM_COLUMN varchar(255), " +
                           "WITHDEFAULTVALUE_ int DEFAULT 0 );");
    }

    @Test
    public void composite_key_entity_has_no_key_column_but_has_one_per_key_member() {
        assertThat(Ddl.of(POSTGRESQL, Entity_WithCompositeKey.class).table())
                .isEqualTo("CREATE TABLE ENTITY_WITHCOMPOSITEKEY_ ( " +
                           "_ID int8 NOT NULL, " +
                           "_VERSION int8 NOT NULL DEFAULT 0, " +
                           "SECOND_ varchar(255) NOT NULL, " +
                           "FIRST_ int8 NOT NULL );");
    }

    @Test
    public void one2one_entity_has_no_key_column_because_its_key_is_the_id() {
        assertThat(Ddl.of(POSTGRESQL, Entity_OneToOne.class).table())
                .isEqualTo("CREATE TABLE ENTITY_ONETOONE_ ( _ID int8 NOT NULL, _VERSION int8 NOT NULL DEFAULT 0 );");
    }

    @Test
    public void union_typed_property_yields_a_nullable_column_per_union_member() {
        assertThat(Ddl.of(POSTGRESQL, Entity_WithUnion.class).table())
                .isEqualTo("CREATE TABLE ENTITY_WITHUNION_ ( " +
                           "_ID int8 NOT NULL, " +
                           "KEY_ varchar(255) NOT NULL, " +
                           "_VERSION int8 NOT NULL DEFAULT 0, " +
                           "PLACE__SIMPLE int8, " +
                           "PLACE__REFS int8 );");
    }

    @Test
    public void union_typed_property_yields_a_computed_column_with_an_index_on_SQL_Server() {
        assertThat(Ddl.of(MSSQL, Entity_WithUnion.class).tableDdl().getColumnDefinition("place").maybeExpression)
                .contains("CASE WHEN PLACE__SIMPLE IS NOT NULL THEN PLACE__SIMPLE WHEN PLACE__REFS IS NOT NULL THEN PLACE__REFS END");
        assertThat(Ddl.of(MSSQL, Entity_WithUnion.class).table())
                .isEqualTo("CREATE TABLE ENTITY_WITHUNION_ ( " +
                           "_ID bigint NOT NULL, " +
                           "KEY_ varchar(255) NOT NULL, " +
                           "_VERSION bigint NOT NULL DEFAULT 0, " +
                           "PLACE_ AS (CASE WHEN PLACE__SIMPLE IS NOT NULL THEN PLACE__SIMPLE WHEN PLACE__REFS IS NOT NULL THEN PLACE__REFS END), " +
                           "PLACE__SIMPLE bigint, " +
                           "PLACE__REFS bigint );");
        assertThat(Ddl.of(MSSQL, Entity_WithUnion.class).indexes())
                .contains("CREATE INDEX I_ENTITY_WITHUNION__PLACE_ ON ENTITY_WITHUNION_(PLACE_ ASC)");
    }

    @Test
    public void component_typed_property_yields_a_column_per_component() {
        assertThat(Ddl.of(POSTGRESQL, Entity_WithRichText.class).table())
                .isEqualTo("CREATE TABLE ENTITY_WITHRICHTEXT_ ( " +
                           "_ID int8 NOT NULL, " +
                           "KEY_ varchar(255) NOT NULL, " +
                           "_VERSION int8 NOT NULL DEFAULT 0, " +
                           "NOTE_FORMATTEDTEXT text, " +
                           "NOTE_CORETEXT text, " +
                           "NOTE_SEARCHTEXT varchar(255) );");
    }

    @Test
    public void desc_is_mapped_to_a_column_only_for_entities_that_expose_it_with_DescTitle() {
        assertThat(Ddl.of(POSTGRESQL, Entity_WithDesc.class).table()).contains("DESC_ varchar(255)");
        assertThat(Ddl.of(POSTGRESQL, Entity_Simple.class).table()).doesNotContain("DESC_");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Primary keys
    // ------------------------------------------------------------------------------------------------------------

    @Test
    public void primary_key_is_generated_for_the_id_column() {
        assertThat(Ddl.of(H2, Entity_Simple.class).pk())
                .isEqualTo("ALTER TABLE ENTITY_SIMPLE_ ADD CONSTRAINT PK_ENTITY_SIMPLE__ID PRIMARY KEY (_ID);");
        assertThat(Ddl.of(H2, Entity_WithRefs.class).pk())
                .isEqualTo("ALTER TABLE REFS_TABLE ADD CONSTRAINT PK_REFS_TABLE_ID PRIMARY KEY (_ID);");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Foreign keys
    // ------------------------------------------------------------------------------------------------------------

    @Test
    public void foreign_keys_are_generated_for_columns_of_persistent_entity_types() {
        assertThat(Ddl.of(H2, Entity_WithRefs.class).fks())
                .containsExactly(
                        "ALTER TABLE REFS_TABLE ADD CONSTRAINT FK_REFS_TABLE_SIMPLE_ FOREIGN KEY (SIMPLE_) REFERENCES ENTITY_SIMPLE_ (_ID);",
                        "ALTER TABLE REFS_TABLE ADD CONSTRAINT FK_REFS_TABLE_OTHER_ FOREIGN KEY (OTHER_) REFERENCES ENTITY_SIMPLE_ (_ID);");
    }

    @Test
    public void foreign_keys_are_generated_for_each_union_member_column() {
        assertThat(Ddl.of(H2, Entity_WithUnion.class).fks())
                .containsExactly(
                        "ALTER TABLE ENTITY_WITHUNION_ ADD CONSTRAINT FK_ENTITY_WITHUNION__PLACE__SIMPLE FOREIGN KEY (PLACE__SIMPLE) REFERENCES ENTITY_SIMPLE_ (_ID);",
                        "ALTER TABLE ENTITY_WITHUNION_ ADD CONSTRAINT FK_ENTITY_WITHUNION__PLACE__REFS FOREIGN KEY (PLACE__REFS) REFERENCES REFS_TABLE (_ID);");
    }

    @Test
    public void one2one_entity_has_a_foreign_key_on_its_id_column() {
        assertThat(Ddl.of(H2, Entity_OneToOne.class).fks())
                .containsExactly("ALTER TABLE ENTITY_ONETOONE_ ADD CONSTRAINT FK_ENTITY_ONETOONE___ID FOREIGN KEY (_ID) REFERENCES ENTITY_SIMPLE_ (_ID);");
    }

    @Test
    public void no_foreign_keys_are_generated_for_an_entity_without_references() {
        assertThat(Ddl.of(H2, Entity_Simple.class).fks()).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Indexes
    // ------------------------------------------------------------------------------------------------------------

    @Test
    public void simple_key_yields_a_unique_index_named_after_the_table() {
        assertThat(Ddl.of(H2, Entity_Simple.class).indexes())
                .containsExactly("CREATE UNIQUE INDEX KUI_ENTITY_SIMPLE_ ON ENTITY_SIMPLE_(KEY_);");
    }

    @Test
    public void composite_key_yields_a_unique_index_over_key_members_ordered_by_CompositeKeyMember() {
        // Key members are declared in the opposite order in `Entity_WithCompositeKey`.
        assertThat(Ddl.of(H2, Entity_WithCompositeKey.class).indexes())
                .startsWith("CREATE UNIQUE INDEX KUI_ENTITY_WITHCOMPOSITEKEY_ ON ENTITY_WITHCOMPOSITEKEY_(FIRST_, SECOND_);");
    }

    @Test
    public void property_of_a_persistent_entity_type_yields_a_non_unique_ascending_index() {
        assertThat(Ddl.of(H2, Entity_WithRefs.class).indexes())
                .containsExactly(
                        "CREATE UNIQUE INDEX KUI_REFS_TABLE ON REFS_TABLE(KEY_);",
                        "CREATE INDEX I_REFS_TABLE_SIMPLE_ ON REFS_TABLE(SIMPLE_ ASC)",
                        "CREATE INDEX I_REFS_TABLE_OTHER_ ON REFS_TABLE(OTHER_ ASC)");
    }

    @Test
    public void createdBy_and_lastUpdatedBy_are_not_indexed_while_other_entity_typed_properties_are() {
        assertThat(Ddl.of(H2, Entity_Persistent.class).indexes())
                .containsExactly(
                        "CREATE UNIQUE INDEX KUI_ENTITY_PERSISTENT_ ON ENTITY_PERSISTENT_(KEY_);",
                        "CREATE INDEX I_ENTITY_PERSISTENT__ASSIGNEE_ ON ENTITY_PERSISTENT_(ASSIGNEE_ ASC)");
    }

    @Test
    public void union_typed_property_yields_an_index_per_union_member() {
        assertThat(Ddl.of(H2, Entity_WithUnion.class).indexes())
                .contains("CREATE INDEX I_ENTITY_WITHUNION__PLACE__SIMPLE ON ENTITY_WITHUNION_(PLACE__SIMPLE ASC)",
                          "CREATE INDEX I_ENTITY_WITHUNION__PLACE__REFS ON ENTITY_WITHUNION_(PLACE__REFS ASC)");
    }

    @Test
    public void only_the_search_text_component_of_a_RichText_property_is_indexed() {
        assertThat(Ddl.of(H2, Entity_WithRichText.class).indexes())
                .containsExactly(
                        "CREATE UNIQUE INDEX KUI_ENTITY_WITHRICHTEXT_ ON ENTITY_WITHRICHTEXT_(KEY_);",
                        "CREATE INDEX I_ENTITY_WITHRICHTEXT__NOTE_SEARCHTEXT ON ENTITY_WITHRICHTEXT_(NOTE_SEARCHTEXT ASC)");
    }

    @Test
    public void auditDate_of_an_audit_entity_is_indexed_in_descending_order() {
        assertThat(Ddl.of(H2, Entity_Audit.class).indexes())
                .containsExactly(
                        "CREATE UNIQUE INDEX KUI_ENTITY_AUDIT_ ON ENTITY_AUDIT_(AUDITEDENTITY_, AUDITEDVERSION_);",
                        "CREATE INDEX I_ENTITY_AUDIT__AUDITEDENTITY_ ON ENTITY_AUDIT_(AUDITEDENTITY_ ASC)",
                        "CREATE INDEX I_ENTITY_AUDIT__AUDITDATE_ ON ENTITY_AUDIT_(AUDITDATE_ DESC)",
                        "CREATE INDEX I_ENTITY_AUDIT__AUDITUSER_ ON ENTITY_AUDIT_(AUDITUSER_ ASC)");
    }

    @Test
    public void unique_boolean_property_is_uniquely_indexed_for_the_Y_value_only() {
        assertThat(Ddl.of(H2, Entity_WithUniqueProps.class).indexes())
                .contains("CREATE UNIQUE INDEX UI_ENTITY_WITHUNIQUEPROPS__BOOLEANUNIQUE_ ON ENTITY_WITHUNIQUEPROPS_(BOOLEANUNIQUE_) WHERE (BOOLEANUNIQUE_ = 'Y');");
    }

    /// A unique index on a nullable column must permit several `NULL` values, which requires a partial index.
    /// Only SQL Server and PostgreSQL are known to support one, so for any other RDBMS the index is skipped.
    ///
    @Test
    public void unique_nullable_property_is_indexed_only_where_partial_indices_are_supported() {
        final var expected = "CREATE UNIQUE INDEX UI_ENTITY_WITHUNIQUEPROPS__NULLABLEUNIQUE_ ON ENTITY_WITHUNIQUEPROPS_(NULLABLEUNIQUE_) WHERE (NULLABLEUNIQUE_ IS NOT NULL);";
        assertThat(Ddl.of(POSTGRESQL, Entity_WithUniqueProps.class).indexes()).contains(expected);
        assertThat(Ddl.of(MSSQL, Entity_WithUniqueProps.class).indexes()).contains(expected);
        assertThat(Ddl.of(H2, Entity_WithUniqueProps.class).indexes()).doesNotContain(expected);
    }

    /// SQL Server cannot index a column of an unbounded length, so such an index is skipped.
    /// PostgreSQL has no such limitation.
    ///
    @Test
    public void index_is_skipped_for_a_column_type_that_the_RDBMS_cannot_index() {
        assertThat(Ddl.of(MSSQL, Entity_WithUniqueProps.class).indexes())
                .noneMatch(sql -> sql.contains("UNBOUNDEDUNIQUE_"));
        assertThat(Ddl.of(POSTGRESQL, Entity_WithUniqueProps.class).indexes())
                .contains("CREATE UNIQUE INDEX UI_ENTITY_WITHUNIQUEPROPS__UNBOUNDEDUNIQUE_ ON ENTITY_WITHUNIQUEPROPS_(UNBOUNDEDUNIQUE_) WHERE (UNBOUNDEDUNIQUE_ IS NOT NULL);");
    }

    @Test
    public void index_name_is_derived_from_the_table_and_column_names() {
        final var ddl = Ddl.of(H2, Entity_WithRefs.class).tableDdl();
        assertThat(ddl.getIndexName("simple")).isEqualTo("I_REFS_TABLE_SIMPLE_");
        assertThat(ddl.getIndexName(ddl.getColumnDefinition("withCustomColumnName"))).isEqualTo("I_REFS_TABLE_OTHER_");
    }

    @Test
    public void expression_index_is_created_for_union_typed_properties_on_PostgreSQL_only() {
        final var unionExprIndex = "CREATE INDEX I_ENTITY_WITHUNION__PLACE_ ON ENTITY_WITHUNION_" +
                      "((CASE WHEN PLACE__SIMPLE IS NOT NULL THEN PLACE__SIMPLE WHEN PLACE__REFS IS NOT NULL THEN PLACE__REFS END) ASC)";
        assertThat(Ddl.of(POSTGRESQL, Entity_WithUnion.class).indexes()).contains(unionExprIndex);
        assertThat(Ddl.of(MSSQL, Entity_WithUnion.class).indexes())
                .doesNotContain(unionExprIndex);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Column definitions
    // ------------------------------------------------------------------------------------------------------------

    @Test
    public void column_definitions_are_addressable_by_a_property_path() {
        final var ddl = Ddl.of(H2, Entity_WithRichText.class).tableDdl();
        assertThat(ddl.getColumnDefinition("note.searchText").name).isEqualTo("NOTE_SEARCHTEXT");
        assertThat(ddl.getColumnDefinitionOpt("note.searchText")).isPresent();
        assertThat(ddl.getColumnDefinitionOpt("note")).isEmpty();
    }

    @Test
    public void requesting_a_column_definition_for_an_unmapped_property_is_an_error() {
        final var ddl = Ddl.of(H2, Entity_Simple.class).tableDdl();
        assertThatThrownBy(() -> ddl.getColumnDefinition("thereIsNoSuchProperty"))
                .isInstanceOf(NoSuchPropertyException.class);
    }

}
