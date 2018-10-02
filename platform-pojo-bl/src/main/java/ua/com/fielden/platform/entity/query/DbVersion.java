package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;

public enum DbVersion {
    MSSQL(CaseSensitivity.INSENSITIVE), 
    ORACLE(CaseSensitivity.SENSITIVE), 
    MYSQL(CaseSensitivity.INSENSITIVE), 
    H2(CaseSensitivity.SENSITIVE),
    POSTGRESQL(CaseSensitivity.SENSITIVE) {
        private static final String ILIKE = "ILIKE";

        @Override
        protected String prepLikeOperand(final String value, final boolean caseInsensitive) {
            return value;
        }

        @Override
        protected String likeOperator(boolean caseInsensitive) {
            return caseInsensitive ? ILIKE : super.likeOperator(caseInsensitive);
        }
    };

    public final CaseSensitivity caseSensitivity;
    private static final String LIKE = "LIKE";
    private static final String NOT = "NOT ";

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

    DbVersion(final CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
    }

    public enum CaseSensitivity {
        INSENSITIVE, SENSITIVE;
    }
}
