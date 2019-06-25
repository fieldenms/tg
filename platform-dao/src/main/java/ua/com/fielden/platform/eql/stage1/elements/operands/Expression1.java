package ua.com.fielden.platform.eql.stage1.elements.operands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.CompoundSingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class Expression1 implements ISingleOperand1<Expression2> {

    private final ISingleOperand1<? extends ISingleOperand2> first;

    private final List<CompoundSingleOperand1> items;

    public Expression1(final ISingleOperand1<? extends ISingleOperand2> first, final List<CompoundSingleOperand1> items) {
        this.first = first;
        this.items = items;
    }

    @Override
    public TransformationResult<Expression2> transform(final PropsResolutionContext resolutionContext) {
        final List<CompoundSingleOperand2> transformed = new ArrayList<>();
        final TransformationResult<? extends ISingleOperand2> firstTransformationResult = first.transform(resolutionContext);
        PropsResolutionContext currentResolutionContext = firstTransformationResult.getUpdatedContext();
        for (final CompoundSingleOperand1 item : items) {
            final TransformationResult<? extends ISingleOperand2> itemTransformationResult = item.operand.transform(currentResolutionContext);
            transformed.add(new CompoundSingleOperand2(itemTransformationResult.getItem(), item.operator));
            currentResolutionContext = itemTransformationResult.getUpdatedContext();
        }
        return new TransformationResult<Expression2>(new Expression2(firstTransformationResult.getItem(), transformed), currentResolutionContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Expression1)) {
            return false;
        }
        final Expression1 other = (Expression1) obj;
        
        return Objects.equals(first, other.first) && Objects.equals(items, other.items);
    }
}