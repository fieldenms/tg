package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public class LikePredicate3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final LikeOptions options;

    public LikePredicate3(final ISingleOperand3 leftOperand, final ISingleOperand3 rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.options = options;
    }

    @Override
    public String sql(final IDomainMetadata metadata) {
        return metadata.dbVersion().likeSql(options.negated, operandToSqlAsString(metadata, leftOperand), rightOperand.sql(metadata), options.caseInsensitive);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + options.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LikePredicate3)) {
            return false;
        }

        final LikePredicate3 other = (LikePredicate3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }

}
