package ua.com.fielden.platform.eql.dbschema;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.eql.dbschema.test_entities.Entity_WithIdOverrideAsCompositeKey;
import ua.com.fielden.platform.eql.dbschema.test_entities.Entity_WithIdOverrideMapToEmpty;
import ua.com.fielden.platform.eql.dbschema.test_entities.Entity_WithIdOverrideMapToUnderscoreId;
import ua.com.fielden.platform.eql.dbschema.test_entities.Entity_WithIdOverrideMapToWrong;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;

/// Tests for the handling of `@IsProperty`-overridden `id` field in [TableDdl].
///
public class TableDdlIdOverrideTest {

    private static final IDbVersionProvider dbVersionProvider = constantDbVersion(H2);
    private static final HibernateTypeMappings hibernateTypeMappings = new Provider(dbVersionProvider).get();
    private static final Dialect dialect = new H2Dialect();
    private static final ColumnDefinitionExtractor extractor = new ColumnDefinitionExtractor(hibernateTypeMappings, dialect);

    @Test
    public void overriding_id_with_MapTo_underscore_ID_does_not_cause_duplicate_column() {
        final var ddl = new TableDdl(extractor, Entity_WithIdOverrideMapToUnderscoreId.class);
        // The default `_ID` column is generated exactly once.
        assertThat(ddl.getColumnDefinition(ID).name).isEqualTo("_ID");
    }

    @Test
    public void overriding_id_with_a_non_default_MapTo_value_is_rejected() {
        assertThatThrownBy(() -> new TableDdl(extractor, Entity_WithIdOverrideMapToWrong.class))
            .isInstanceOf(DbSchemaException.class)
            .hasMessageContaining("WRONG_COL")
            .hasMessageContaining("only @MapTo(\"_ID\") is permitted");
    }

    @Test
    public void overriding_id_with_an_empty_MapTo_value_is_rejected() {
        assertThatThrownBy(() -> new TableDdl(extractor, Entity_WithIdOverrideMapToEmpty.class))
            .isInstanceOf(DbSchemaException.class)
            .hasMessageContaining("@MapTo without a value")
            .hasMessageContaining("only @MapTo(\"_ID\") is permitted");
    }

    @Test
    public void overriding_id_as_the_sole_composite_key_member_emits_no_composite_index() {
        final var ddl = new TableDdl(extractor, Entity_WithIdOverrideAsCompositeKey.class);
        // The overridden `id`'s `@CompositeKeyMember` is dismissed, so no column feeds the composite unique index.
        // It must be skipped entirely rather than emitted over an empty column list, e.g. `... ON T()` (invalid SQL).
        final var indices = ddl.createIndicesSchema(dialect);
        assertThat(indices).noneMatch(sql -> sql.contains("KUI_")); // `KUI_` prefixes the composite-key unique index
        assertThat(indices).noneMatch(sql -> sql.contains("()"));   // no index is generated over an empty column list
    }

}
