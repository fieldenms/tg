package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.asRuntime;

import ua.com.fielden.platform.reflection.Reflector;

public enum DbVersion {
    MSSQL(CaseSensitivity.INSENSITIVE, " AS ") {
        @Override
        public String nextSequenceValSql() {
            return "NEXT VALUE FOR %s".formatted(idSequenceName());
        }
    }, 
    ORACLE(CaseSensitivity.SENSITIVE, " ") {
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
    MYSQL(CaseSensitivity.INSENSITIVE, " AS ") {
        @Override
        public String nextSequenceValSql() {
            throw new UnsupportedOperationException("Sequences are not supported for MySQL.");
        }
    }, 
    H2(CaseSensitivity.SENSITIVE, " AS ") {
        @Override
        public String nextSequenceValSql() {
            return "NEXT VALUE FOR %s".formatted(idSequenceName());
        }
    },
    POSTGRESQL(CaseSensitivity.SENSITIVE, " AS ") {
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

    };

    public final CaseSensitivity caseSensitivity;
    private static final String LIKE = "LIKE";
    private static final String NOT = "NOT ";
    public final String AS;

    /**
     * A flag that provides a way to support generation of aliases as part of SQL statements, which may lead for performance degradation due to slow parsing.
     * Refer to issue https://github.com/fieldenms/tg/issues/1215 for more details.
     */
    private static boolean GEN_ALIAS_COMMENTS = false;

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
     * Processes the operand value to ensure case insensitive affect if required.
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

    DbVersion(final CaseSensitivity caseSensitivity, final String AS) {
        this.caseSensitivity = caseSensitivity;
        this.AS = AS;
    }

    public enum CaseSensitivity {
        INSENSITIVE, SENSITIVE;
    }
    
    public String idColumnName() {
        return "_ID";
    }

    public String versionColumnName() {
        return "_VERSION";
    }
    
    private static String ID_SEQUENCE_NAME = "TG_ENTITY_ID_SEQ";
    public String idSequenceName() {
        return ID_SEQUENCE_NAME;
    }
    
    public abstract String nextSequenceValSql();

}