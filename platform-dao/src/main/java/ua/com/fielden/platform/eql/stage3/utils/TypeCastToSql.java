package ua.com.fielden.platform.eql.stage3.utils;

import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsBoolean;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsDecimal;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsInteger;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsString;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;

import static org.hibernate.mapping.Column.*;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.*;

/**
 * Utilities to generate SQL type casting expressions from {@link ITypeCast}.
 */
public final class TypeCastToSql {

    private TypeCastToSql() {}

    /**
     * Generates an SQL type casting expression.
     *
     * @param expression  expression that is cast
     * @param typeCast  the type we are casting to
     * @param metadata  runtime context
     */
    public static String typeCastToSql(final String expression, final ITypeCast typeCast, final EqlDomainMetadata metadata) {
        // NOTE: Dialect.cast(...) can return invalid SQL in Hibernate 5.4.33; don't use it
        final String typeSql = switch (typeCast) {
            case AsString cast -> metadata.dialect.getTypeName(jdbcSqlTypeFor(H_STRING), cast.length, DEFAULT_PRECISION, DEFAULT_SCALE);
            case AsInteger $ -> metadata.dialect.getTypeName(jdbcSqlTypeFor(H_INTEGER), DEFAULT_LENGTH, DEFAULT_PRECISION, DEFAULT_SCALE);
            case AsBoolean $ -> metadata.dialect.getTypeName(jdbcSqlTypeFor(H_STRING), 1, DEFAULT_PRECISION, DEFAULT_SCALE);
            case AsDecimal cast -> metadata.dialect.getTypeName(jdbcSqlTypeFor(H_BIGDECIMAL), DEFAULT_LENGTH, cast.precision, cast.scale);
        };

        return metadata.dbVersion.castSql(expression, typeSql);
    }

}
