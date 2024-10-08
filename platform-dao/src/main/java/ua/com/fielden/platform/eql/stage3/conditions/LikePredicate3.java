package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public record LikePredicate3(ISingleOperand3 leftOperand, ISingleOperand3 rightOperand, LikeOptions options)
        implements ICondition3, ToString.IFormattable
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return dbVersion.likeSql(options.negated,
                                 operandToSqlAsString(metadata, dbVersion, leftOperand),
                                 rightOperand.sql(metadata, dbVersion),
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
                .add("left", leftOperand)
                .add("right", rightOperand)
                .$();
    }

}
