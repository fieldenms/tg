package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class Expression3 extends AbstractSingleOperand3 {

    public final ISingleOperand3 firstOperand;
    private final List<CompoundSingleOperand3> otherOperands;

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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + firstOperand.hashCode();
        result = prime * result + otherOperands.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Expression3 that
                  && Objects.equals(firstOperand, that.firstOperand)
                  && Objects.equals(otherOperands, that.otherOperands)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("first", firstOperand)
                .add("rest", otherOperands);
    }

}
