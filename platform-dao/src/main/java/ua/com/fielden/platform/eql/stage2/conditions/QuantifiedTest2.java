package ua.com.fielden.platform.eql.stage2.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.SubQuery2;
import ua.com.fielden.platform.eql.stage3.conditions.QuantifiedTest3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.SubQuery3;

public class QuantifiedTest2 extends AbstractCondition2<QuantifiedTest3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final SubQuery2 rightOperand;
    public final Quantifier quantifier;
    public final ComparisonOperator operator;

    public QuantifiedTest2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final SubQuery2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public TransformationResult<QuantifiedTest3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResult<SubQuery3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        
        return new TransformationResult<QuantifiedTest3>(new QuantifiedTest3(leftOperandTr.item, operator, quantifier, rightOperandTr.item), rightOperandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(leftOperand.collectProps());
        result.addAll(rightOperand.collectProps());
        return result;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + operator.hashCode();
        result = prime * result + quantifier.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof QuantifiedTest2)) {
            return false;
        }
        
        final QuantifiedTest2 other = (QuantifiedTest2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}