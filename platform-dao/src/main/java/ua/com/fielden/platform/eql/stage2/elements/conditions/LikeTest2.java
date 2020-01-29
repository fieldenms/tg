package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.LikeTest3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class LikeTest2 extends AbstractCondition2<LikeTest3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final ISingleOperand2<? extends ISingleOperand3> rightOperand;
    public final LikeOptions options;

    public LikeTest2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ISingleOperand2<? extends ISingleOperand3> rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.options = options;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    public TransformationResult<LikeTest3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResult<? extends ISingleOperand3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        return new TransformationResult<LikeTest3>(new LikeTest3(leftOperandTr.item, rightOperandTr.item, options), rightOperandTr.updatedContext);
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
        result = prime * result + options.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LikeTest2)) {
            return false;
        }

        final LikeTest2 other = (LikeTest2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }
}