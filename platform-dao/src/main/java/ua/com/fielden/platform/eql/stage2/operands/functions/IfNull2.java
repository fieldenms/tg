package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.IfNull3;

public class IfNull2 extends TwoOperandsFunction2<IfNull3> {

    public IfNull2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2, operand1.type());
    }

    @Override
    public TransformationResult2<IfNull3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult2<? extends ISingleOperand3> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult2<>(new IfNull3(firstOperandTransformationResult.item, secondOperandTransformationResult.item, type), secondOperandTransformationResult.updatedContext);
    }
    
    @Override
    public boolean isNonnullableEntity() {
        // There is no point in checking first operand for null value if it's nonnullableEntity = true. Hence:
        // if second operand is nonnullableEntity then --
        //  - if first operand is not null and is returned (it turns out to be nonnullableEntity) the result will be true (as it should be),
        //  - if first operand is null and second operand is returned the result will also be true (as it should be);
        // if second operand is not nonnullableEntity then --
        //  - if first operand is null and second operand is returned the result will be false (as should be);
        //  - if first operand is not null and is returned the result will be false (but actually should be true, but there is no way to infer this from the available information).
        return operand2.isNonnullableEntity();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + IfNull2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof IfNull2;
    }
}