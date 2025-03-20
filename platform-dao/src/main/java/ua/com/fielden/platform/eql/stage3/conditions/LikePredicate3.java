package ua.com.fielden.platform.eql.stage3.conditions;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Value3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public record LikePredicate3(ISingleOperand3 matchOperand, ISingleOperand3 patternOperand, LikeOptions options)
        implements ICondition3, ToString.IFormattable
{

    private static final Logger LOGGER = getLogger();

    private static final String WARN_ESCAPING_NOT_SUPPORTED =
            "Escaping of special characters in LIKE clauses is not supported for database [%s].";

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return dbVersion.likeSql(options.negated,
                                 operandToSqlAsString(metadata, dbVersion, matchOperand),
                                 escapeSql(patternOperand, patternOperand.sql(metadata, dbVersion), dbVersion),
                                 options.caseInsensitive);
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

    private String escapeSql(final ISingleOperand3 operand, final String sql, final DbVersion dbVersion) {
        return switch (operand) {
            // Literal strings are escaped during stage2.
            case Value3 $ -> sql;
            default -> switch (dbVersion) {
                case POSTGRESQL -> foldReplaceSql(sql, POSTGRESQL);
                case MSSQL -> foldReplaceSql(sql, MSSQL);
                default -> {
                    LOGGER.warn(() -> WARN_ESCAPING_NOT_SUPPORTED.formatted(dbVersion));
                    yield sql;
                }
            };
        };
    }

    private static String foldReplaceSql(
            final CharSequence initSql,
            final DbVersion dbVersion)
    {
        // Imperative implementation to avoid zipping and creating pairs.
        String accSql = initSql.toString();
        for (int i = 0; i < dbVersion.searchList.size(); i++) {
            accSql = dbVersion.replaceSql(accSql, dbVersion.searchList.get(i), dbVersion.replacementList.get(i));
        }
        return accSql;
    }

}
