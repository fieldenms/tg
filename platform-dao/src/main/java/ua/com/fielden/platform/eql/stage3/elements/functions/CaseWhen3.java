package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.Pair;

public class CaseWhen3 implements ISingleOperand3 {

    private List<Pair<ICondition3, ISingleOperand3>> whenThenPairs = new ArrayList<Pair<ICondition3, ISingleOperand3>>();
    private final ISingleOperand3 elseOperand;

    public CaseWhen3(final List<Pair<ICondition3, ISingleOperand3>> whenThenPairs, final ISingleOperand3 elseOperand) {
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
        result = prime * result + ((whenThenPairs == null) ? 0 : whenThenPairs.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof CaseWhen3)) {
            return false;
        }

        final CaseWhen3 other = (CaseWhen3) obj;

        return Objects.equals(whenThenPairs, other.whenThenPairs) && Objects.equals(elseOperand, other.elseOperand);
    }
}