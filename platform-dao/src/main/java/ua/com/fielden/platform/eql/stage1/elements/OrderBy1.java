package ua.com.fielden.platform.eql.stage1.elements;

import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class OrderBy1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final boolean isDesc;

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this.operand = operand;
        this.isDesc = isDesc;
    }

    public List<OrderBy2> transform(final PropsResolutionContext context) {
        final List<OrderBy2> result = new ArrayList<>();
        final ISingleOperand2<?> operandTr = operand.transform(context);
        if (!(operandTr instanceof EntProp2)) {
            result.add(new OrderBy2(operandTr, isDesc));
        } else {
            final EntProp2 operandProp = (EntProp2) operandTr;
            final int pathLength = operandProp.getPath().size();
            if (!operandProp.getPath().get(pathLength - 1).name.equals(KEY)) {
                result.add(new OrderBy2(operandTr, isDesc));
            } else {
                final String prefix = ((EntProp1) operand).name.equals(KEY) ? "" : ((EntProp1) operand).name.substring(0, ((EntProp1) operand).name.length() - 3);
                final List<String> keyOrderProps = pathLength == 1 && isCompositeEntity(operandProp.source.sourceType()) ? keyPaths(operandProp.source.sourceType())
                        : pathLength > 1 && isCompositeEntity((Class<? extends AbstractEntity<?>>) operandProp.getPath().get(pathLength - 2).javaType())
                                ? keyPaths((Class<? extends AbstractEntity<?>>) operandProp.getPath().get(pathLength - 2).javaType())
                                : emptyList();

                for (final String keyMemberProp : keyOrderProps) {
                    result.add(new OrderBy2((new EntProp1(prefix + keyMemberProp, false)).transform(context), isDesc));
                }

            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBy1)) {
            return false;
        }

        final OrderBy1 other = (OrderBy1) obj;

        return Objects.equals(operand, other.operand) && (isDesc == other.isDesc);
    }
}