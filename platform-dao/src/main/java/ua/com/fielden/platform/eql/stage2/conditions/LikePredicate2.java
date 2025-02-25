package ua.com.fielden.platform.eql.stage2.conditions;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;
import ua.com.fielden.platform.eql.stage3.conditions.LikePredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public record LikePredicate2 (ISingleOperand2<? extends ISingleOperand3> matchOperand,
                              ISingleOperand2<? extends ISingleOperand3> patternOperand,
                              LikeOptions options)
        implements ICondition2<LikePredicate3>, ToString.IFormattable
{

    public static final String[] MSSQL_SEARCH_LIST =  { "["  , "_"   };
    public static final String[] MSSQL_REPLACE_LIST = { "[[]", "[_]" };

    // In PostgreSQL backslash is the default escape character in the LIKE clause, hence its special meaning and the need to be escaped.
    public static final String[] POSTGRESQL_SEARCH_LIST =  { "\\" , "_"  };
    public static final String[] POSTGRESQL_REPLACE_LIST = { "\\\\" , "\\_"};

    @Override
    public boolean ignore() {
        return matchOperand.ignore() || patternOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<LikePredicate3> transform(final TransformationContextFromStage2To3 context) {
        // Escaping of the pattern operand has to be performed at this stage, before the operand is transformed.
        // When a Value2, representing a string, is transformed into Value3, it is subject to parameter substitution,
        // which results in the underlying string being stored externally (in a map of parameters).
        // Therefore, such strings need to be escaped now, before they are "externalised".
        // Pattern operands other than literal strings are also escaped, but in stage 3 (see LikePredicate3).
        final TransformationResultFromStage2To3<? extends ISingleOperand3> matchOperandTr = matchOperand.transform(
                context);
        final TransformationResultFromStage2To3<? extends ISingleOperand3> patternOperandTr = escapeLiteralString(patternOperand, context.dbVersion())
                .transform(matchOperandTr.updatedContext);
        return new TransformationResultFromStage2To3<>(
                new LikePredicate3(matchOperandTr.item, patternOperandTr.item, options), patternOperandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(matchOperand.collectProps());
        result.addAll(patternOperand.collectProps());
        return result;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, matchOperand.collectEntityTypes(), patternOperand.collectEntityTypes());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .addIf("options", options, opts -> opts != LikeOptions.DEFAULT_OPTIONS)
                .add("match", matchOperand)
                .add("pattern", patternOperand)
                .$();
    }

    /**
     * Escapes the specified operand if it is a literal string.
     */
    private ISingleOperand2<? extends ISingleOperand3> escapeLiteralString(
            final ISingleOperand2 operand,
            final DbVersion dbVersion)
    {
        return switch (operand) {
            case Value2 value -> value.map(v -> v instanceof String s ? escapeSqlString(s, dbVersion) : v);
            default -> operand;
        };
    }

    private String escapeSqlString(final String string, final DbVersion dbVersion) {
        return switch (dbVersion) {
            case MSSQL -> StringUtils.replaceEach(string, MSSQL_SEARCH_LIST, MSSQL_REPLACE_LIST);
            case POSTGRESQL -> StringUtils.replaceEach(string, POSTGRESQL_SEARCH_LIST, POSTGRESQL_REPLACE_LIST);
            default -> string;
        };
    }

}
