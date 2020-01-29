package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class ComparisonTest2 extends AbstractCondition2<ComparisonTest3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final ISingleOperand2<? extends ISingleOperand3> rightOperand;
    public final ComparisonOperator operator;

    public ComparisonTest2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ComparisonOperator operator, final ISingleOperand2<? extends ISingleOperand3> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    public TransformationResult<ComparisonTest3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResult<? extends ISingleOperand3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        return new TransformationResult<ComparisonTest3>(new ComparisonTest3(leftOperandTr.item, operator, rightOperandTr.item), rightOperandTr.updatedContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
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
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ComparisonTest2)) {
            return false;
        }
        
        final ComparisonTest2 other = (ComparisonTest2) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
   }
}