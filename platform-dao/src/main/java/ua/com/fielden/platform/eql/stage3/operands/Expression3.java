package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class Expression3 extends AbstractSingleOperand3 {

    public final ISingleOperand3 firstOperand;
    public final List<CompoundSingleOperand3> otherOperands;

    public Expression3(final ISingleOperand3 first, final List<CompoundSingleOperand3> items, final PropType type) {
        super(type);
        this.firstOperand = first;
        this.otherOperands = items;
    }

    public boolean isSingleOperandExpression() {
        return otherOperands.isEmpty();
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return isSingleOperandExpression()
                ? firstOperand.sql(metadata, dbVersion)
                : "("
                  + firstOperand.sql(metadata, dbVersion)
                  + otherOperands.stream().map(co -> co.sql(metadata, dbVersion)).collect(joining())
                  + ")";
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("first", firstOperand)
                .add("rest", otherOperands);
    }

}
