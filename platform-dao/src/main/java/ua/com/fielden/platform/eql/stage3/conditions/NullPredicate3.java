package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record NullPredicate3 (ISingleOperand3 operand, boolean negated) implements ICondition3, ToString.IFormattable {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operand.sql(metadata, dbVersion) + " IS " + (negated ? "NOT" : "") + " NULL";
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("negated", negated)
                .add("operand", operand)
                .$();
    }

}
