package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record OrderBy3 (ISingleOperand3 operand, Yield3 yield, boolean isDesc) implements ToString.IFormattable {

    public OrderBy3(final ISingleOperand3 operand, final boolean isDesc) {
        this(operand, null, isDesc);
    }

    public OrderBy3(final Yield3 yield, final boolean isDesc) {
        this(null, yield, isDesc);
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return (operand != null ? operand.sql(metadata, dbVersion) : yield.column()) + (isDesc ? " DESC" : " ASC");
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .addIfNotNull("operand", operand)
                .addIfNotNull("yield", yield)
                .add("isDesc", isDesc)
                .$();
    }

}
