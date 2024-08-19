package ua.com.fielden.platform.eql.stage3.operands;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;

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
    public String sql(final EqlDomainMetadata metadata) {
        return isSingleOperandExpression() ? firstOperand.sql(metadata) : "(" + firstOperand.sql(metadata) + otherOperands.stream().map(co -> co.sql(metadata)).collect(joining()) +")";
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
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof Expression3)) {
            return false;
        }

        final Expression3 other = (Expression3) obj;

        return Objects.equals(firstOperand, other.firstOperand) && Objects.equals(otherOperands, other.otherOperands);
    }
}
