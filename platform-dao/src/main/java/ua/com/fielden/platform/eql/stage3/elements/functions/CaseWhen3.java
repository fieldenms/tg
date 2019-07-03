package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.Pair;

public class CaseWhen3 extends AbstractFunction3 {

    private List<Pair<ICondition3, ISingleOperand3>> whenThenPairs = new ArrayList<Pair<ICondition3, ISingleOperand3>>();
    private final ISingleOperand3 elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen3(final List<Pair<ICondition3, ISingleOperand3>> whenThenPairs, final ISingleOperand3 elseOperand, final ITypeCast typeCast) {
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        final StringBuffer sb = new StringBuffer();
        sb.append("CASE");
        for (final Pair<ICondition3, ISingleOperand3> whenThen : whenThenPairs) {
            sb.append(" WHEN " + whenThen.getKey().sql(dbVersion) + " THEN " + (typeCast == null ? whenThen.getValue().sql(dbVersion) : typeCast.typecast(whenThen.getValue().sql(dbVersion), dbVersion)));
        }
        if (elseOperand != null) {
            sb.append(" ELSE " + (typeCast == null ? elseOperand.sql(dbVersion) : typeCast.typecast(elseOperand.sql(dbVersion), dbVersion)));
        }
        sb.append(" END");
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
        result = prime * result + ((typeCast == null) ? 0 : typeCast.hashCode());
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

        return Objects.equals(whenThenPairs, other.whenThenPairs) && Objects.equals(elseOperand, other.elseOperand) && Objects.equals(typeCast, other.typeCast);
    }
}