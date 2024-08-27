package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import static java.lang.String.format;

public class CountOf3 extends SingleOperandFunction3 {
    private final boolean distinct;
    
    public CountOf3(final ISingleOperand3 operand, final boolean distinct, final PropType type) {
        super(operand, type);
        this.distinct = distinct;
    }
    
    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String distinctClause = distinct ? "DISTINCT " : "";
        return format("COUNT(%s %s)", distinctClause, operand.sql(metadata, dbVersion));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + (distinct ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof CountOf3 that
                  && distinct == that.distinct
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("distinct", distinct);
    }

}
