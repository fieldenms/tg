package ua.com.fielden.platform.entity.query;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.reflection.Reflector;

import java.util.List;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.asRuntime;

public enum DbVersion {
    MSSQL(CaseSensitivity.INSENSITIVE, " AS ", ImmutableList.of("["  , "_"), ImmutableList.of("[[]", "[_]")) {
        @Override
        public String nextSequenceValSql() {
            return "NEXT VALUE FOR %s".formatted(idSequenceName());
        }

        @Override
        public String addColumnSql(final CharSequence table, final CharSequence columnDefinition) {
            return "ALTER TABLE " + table + " ADD " + columnDefinition;
        }

        @Override
        public String deleteColumnSql(final CharSequence table, final CharSequence columnName) {
            return "ALTER TABLE " + table + " DROP COLUMN " + columnName;
        }

        @Override
        public String alterColumnNullabilitySql(
                final CharSequence tableName,
                final CharSequence columnName,
                final CharSequence columnType,
                final Nullability nullability)
        {
            final String nullabilityString = switch (nullability) {
                case NULL -> "NULL";
                case NOT_NULL -> "NOT NULL";
            };
            return format("ALTER TABLE %s ALTER COLUMN %s %s %s", tableName, columnName, columnType, nullabilityString);
        }

        @Override
        public String dropIndexSql(final CharSequence indexName, final CharSequence tableName, final boolean ifExists) {
            return format("DROP INDEX %s [%s] ON [%s]",
                          ifExists ? "IF EXISTS" : "",
                          indexName, tableName);
        }

        /**
         * If {@code expression} is SQL {@code NULL}, the result of the replace function is also {@code NULL}.
         *
         * @see <a href="https://learn.microsoft.com/en-us/sql/t-sql/functions/replace-transact-sql?view=sql-server-ver16">SQL Server documentation</a>
         */
        public String replaceSql(final CharSequence expression, final CharSequence pattern, final CharSequence replacement) {
            // A single quote can be escaped by doubling it up.
            return format("REPLACE(%s, '%s', '%s')",
                          expression,
                          pattern.toString().replace("'", "''"),
                          replacement.toString().replace("'", "''"));
        }
    },

    ORACLE(CaseSensitivity.SENSITIVE, " ", ImmutableList.of(), ImmutableList.of()) {
        public String idColumnName() {
            return "TG_ID";
        }

        public String versionColumnName() {
            return "TG_VERSION";
        }

        @Override
        public String nextSequenceValSql() {
            return "%s.NEXTVAL".formatted(idSequenceName());
        }
    }, 

    MYSQL(CaseSensitivity.INSENSITIVE, " AS ", ImmutableList.of(), ImmutableList.of()) {
        @Override
        public String nextSequenceValSql() {
            throw new UnsupportedOperationException("Sequences are not supported for MySQL.");
        }
    }, 

    H2(CaseSensitivity.SENSITIVE, " AS ", ImmutableList.of(), ImmutableList.of()) {
        @Override
        public String nextSequenceValSql() {
            return "NEXT VALUE FOR %s".formatted(idSequenceName());
        }
    },

    /**
     * In PostgreSQL backslash is the default escape character in the LIKE clause, hence its special meaning and the need to be escaped.
     */
    POSTGRESQL(CaseSensitivity.SENSITIVE, " AS ", ImmutableList.of("\\" , "_"), ImmutableList.of("\\\\" , "\\_")) {
        private static final String ILIKE = "ILIKE";

        @Override
        protected String prepLikeOperand(final String value, final boolean caseInsensitive) {
            return value;
        }

        @Override
        protected String likeOperator(boolean caseInsensitive) {
            return caseInsensitive ? ILIKE : super.likeOperator(caseInsensitive);
        }

        @Override
        public String nextSequenceValSql() {
            return "NEXTVAL('%s')".formatted(idSequenceName());
        }

        @Override
        public String addColumnSql(final CharSequence table, final CharSequence columnDefinition) {
            return "ALTER TABLE " + table + " ADD COLUMN " + columnDefinition;
        }

        @Override
        public String deleteColumnSql(final CharSequence table, final CharSequence columnName) {
            return "ALTER TABLE " + table + " DROP COLUMN " + columnName;
        }

        @Override
        public String alterColumnNullabilitySql(
                final CharSequence tableName,
                final CharSequence columnName,
                final CharSequence columnType,
                final Nullability nullability)
        {
            return switch (nullability) {
                case NULL -> "ALTER TABLE %s ALTER COLUMN %s DROP NOT NULL".formatted(tableName, columnName);
                case NOT_NULL -> "ALTER TABLE %s ALTER COLUMN %s SET NOT NULL".formatted(tableName, columnName);
            };
        }

        @Override
        public String dropIndexSql(final CharSequence indexName, final CharSequence tableName, final boolean ifExists) {
            return format("DROP INDEX %s %s",
                          ifExists ? "IF EXISTS" : "",
                          indexName);
        }

        /**
         * If {@code expression} is SQL {@code NULL}, the result of the replace function is also {@code NULL}.
         *
         * @see <a href="https://www.postgresql.org/docs/8.2/functions-string.html">PostgreSQL documentation</a>
         */
        public String replaceSql(final CharSequence expression, final CharSequence pattern, final CharSequence replacement) {
            // A single quote can be escaped by doubling it up.
            return format("replace(%s, '%s', '%s')",
                          expression,
                          pattern.toString().replace("'", "''"),
                          replacement.toString().replace("'", "''"));
        }
    };

    public static final String ERR_SEARCH_AND_REPLACEMENT_SIZE = "Search and replacement lists should be of the same size.";
    public final CaseSensitivity caseSensitivity;
    private static final String LIKE = "LIKE";
    private static final String NOT = "NOT ";
    public final String AS;

    /**
     * A flag that provides a way to support generation of aliases as part of SQL statements, which may lead for performance degradation due to slow parsing.
     * Refer to <a href="https://github.com/fieldenms/tg/issues/1215">Issue 1215</a> for more details.
     */
    private static final boolean GEN_ALIAS_COMMENTS = false;

    public static String aliasComment(final String alias) {
        return GEN_ALIAS_COMMENTS ? "/*" + alias + "*/" : " ";
    }
    
    /**
     * Turns on generation of alias comments in SQL expression. This may slow down the application performance.
     * It is strongly recommended not to use this mode for application deployment.
     */
    public static void genAliasComments() {
        try {
            Reflector.assignStatic(DbVersion.class.getDeclaredField("GEN_ALIAS_COMMENTS"), true);
        } catch (final Exception ex) {
            throw asRuntime(ex);
        }
    }

    /**
     * Constructs an SQL expression representing a type cast.
     *
     * @param expression  SQL expression to cast
     * @param type  name of the SQL type which the expression is cast to
     */
    public String castSql(final String expression, final String type) {
        return "CAST (%s AS %s)".formatted(expression, type);
    }

    /**
     * Returns a {@code LIKE} term with its left and right operands, all processed with case sensitivity in mind.
     *
     * @param leftOperand
     * @param rightOperand
     * @param caseInsensitive
     * @return
     */
    public String likeSql(final boolean negation, final String leftOperand, final String rightOperand, final boolean caseInsensitive) {
        final String likeOp = likeOperator(caseInsensitive);
        return format("%s %s %s", prepLikeOperand(leftOperand, caseInsensitive), negation ? NOT + likeOp : likeOp, prepLikeOperand(rightOperand, caseInsensitive));
    }

    /**
     * Processes the operand value to ensure a case-insensitive effect if required.
     *
     * @param value
     * @param caseInsensitive
     * @return
     */
    protected String prepLikeOperand(final String value, final boolean caseInsensitive) {
        if (CaseSensitivity.SENSITIVE == caseSensitivity && caseInsensitive) {
            return format(" UPPER(%s) ", value);
        } else {
            return value;
        }
    }

    /**
     * Returns SQL operator {@code LIKE}, which depending on the RDBMS in use may return a different operator for case insensitive/sensitive requests.
     *
     * @param caseInsensitive -- indicates if the {@code LIKE} should be case insensitive. 
     * @return
     */
    protected String likeOperator(final boolean caseInsensitive) {
        return LIKE;
    }

    DbVersion(final CaseSensitivity caseSensitivity,
              final String AS,
              final List<String> searchList,
              final List<String> replacementList)
    {
        this.caseSensitivity = caseSensitivity;
        this.AS = AS;
        if (searchList.size() != replacementList.size()) {
            throw new IllegalArgumentException(ERR_SEARCH_AND_REPLACEMENT_SIZE);
        }
        this.searchList = ImmutableList.copyOf(searchList);
        this.replacementList = ImmutableList.copyOf(replacementList);
    }

    /**
     * Generates an SQL statement that alters the nullability status of the specified column.
     */
    public String alterColumnNullabilitySql(
            final CharSequence tableName,
            final CharSequence columnName,
            final CharSequence columnType,
            final Nullability nullability)
    {
        throw new UnsupportedOperationException(this.toString());
    }

    public enum Nullability { NULL, NOT_NULL }

    public enum CaseSensitivity {
        INSENSITIVE, SENSITIVE;
    }
    
    public String idColumnName() {
        return "_ID";
    }

    public String versionColumnName() {
        return "_VERSION";
    }
    
    public static final String ID_SEQUENCE_NAME = "TG_ENTITY_ID_SEQ";
    public String idSequenceName() {
        return ID_SEQUENCE_NAME;
    }
    
    public abstract String nextSequenceValSql();

    /**
     * Generates an SQL statement that adds a column with the specified definition to the specified table.
     */
    public String addColumnSql(final CharSequence table, final CharSequence columnDefinition) {
        throw new UnsupportedOperationException(this.toString());
    }

    /**
     * Generates an SQL statement that deletes the specified column from the specified table.
     */
    public String deleteColumnSql(final CharSequence table, final CharSequence columnName) {
        throw new UnsupportedOperationException(this.toString());
    }

    public String dropIndexSql(final CharSequence indexName, final CharSequence tableName, final boolean ifExists) {
        throw new UnsupportedOperationException(this.toString());
    }

    /**
     * Generates an SQL statement that has the effect of calling function {@code REPLACE(expression, pattern, replacement)},
     * which produces a string with the contents of {@code expression}, but with all occurrences of {@code pattern} replaced
     * by {@code replacement}.
     */
    public String replaceSql(final CharSequence expression, final CharSequence pattern, final CharSequence replacement) {
        throw new UnsupportedOperationException(this.toString());
    }

    /**
     * Corresponding elements from these two lists represent replacement rules used for escaping search strings.
     */
    public final List<String> searchList;
    public final List<String> replacementList;
}
