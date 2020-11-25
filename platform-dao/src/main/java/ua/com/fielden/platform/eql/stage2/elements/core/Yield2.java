package ua.com.fielden.platform.eql.stage2.elements.core;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.core.Yield3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Yield2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final String alias;
    public final boolean hasRequiredHint;

    public Yield2(final ISingleOperand2<? extends ISingleOperand3> operand, final String alias, final boolean hasRequiredHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasRequiredHint = hasRequiredHint;
    }

    public Class<?> javaType() {
        return operand.type();
    }

    public TransformationResult<Yield3> transform(final TransformationContext context) {
        final Object hibType = operand instanceof EntProp2 ? ((EntProp2) operand).hibType : null;
        final Class<?> type = operand instanceof EntProp2 ? ((EntProp2) operand).type : null;
        final TransformationContext newContext = context.cloneWithNextSqlId();
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(newContext);
        return new TransformationResult<Yield3>(new Yield3(operandTransformationResult.item, alias, newContext.sqlId, operand.isHeader(), type, hibType), operandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
        result = prime * result + (hasRequiredHint ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Yield2)) {
            return false;
        }
        
        final Yield2 other = (Yield2) obj;
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && (hasRequiredHint == other.hasRequiredHint);
    }
}