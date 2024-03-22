package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.types.tuples.T2;

public class CaseWhen3 extends AbstractFunction3 {

    private List<T2<ICondition3, ISingleOperand3>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand3 elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen3(final List<T2<ICondition3, ISingleOperand3>> whenThenPairs, final ISingleOperand3 elseOperand, final ITypeCast typeCast, final PropType type) {
        super(type);
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        final StringBuffer sb = new StringBuffer();
        sb.append("CASE");
        for (final T2<ICondition3, ISingleOperand3> whenThen : whenThenPairs) {
            sb.append(format(" WHEN %s THEN %s", whenThen._1.sql(metadata), getOperandSql(whenThen._2, metadata)));
        }
        if (elseOperand != null) {
            sb.append(format(" ELSE %s", getOperandSql(elseOperand, metadata)));
        }
        sb.append(" END");
        return sb.toString();
    }
    
    private String getOperandSql(final ISingleOperand3 operand, final EqlDomainMetadata metadata) {
        return typeCast == null ? operand.sql(metadata) : typeCast.typecast(operand.sql(metadata), metadata.dbVersion);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
        result = prime * result + ((typeCast == null) ? 0 : typeCast.hashCode());
        result = prime * result + whenThenPairs.hashCode();
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
        
        if (!(obj instanceof CaseWhen3)) {
            return false;
        }

        final CaseWhen3 other = (CaseWhen3) obj;

        return Objects.equals(whenThenPairs, other.whenThenPairs) && Objects.equals(elseOperand, other.elseOperand) && Objects.equals(typeCast, other.typeCast);
    }
}
