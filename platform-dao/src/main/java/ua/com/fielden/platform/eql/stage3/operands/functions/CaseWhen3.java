package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ua.com.fielden.platform.eql.stage3.utils.TypeCastToSql.typeCastToSql;

public class CaseWhen3 extends AbstractFunction3 {

    private List<T2<ICondition3, ISingleOperand3>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand3 elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen3(final List<T2<ICondition3, ISingleOperand3>> whenThenPairs, final ISingleOperand3 elseOperand, final ITypeCast typeCast, final PropType type) {
        super(type);
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
        validateSelf();
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final StringBuffer sb = new StringBuffer();
        sb.append("CASE");
        for (final T2<ICondition3, ISingleOperand3> whenThen : whenThenPairs) {
            sb.append(String.format(" WHEN %s THEN %s", whenThen._1.sql(metadata, dbVersion), getOperandSql(whenThen._2, metadata, dbVersion)));
        }
        if (elseOperand != null) {
            sb.append(String.format(" ELSE %s", getOperandSql(elseOperand, metadata, dbVersion)));
        }
        sb.append(" END");
        return sb.toString();
    }
    
    private String getOperandSql(final ISingleOperand3 operand, final IDomainMetadata metadata, final DbVersion dbVersion) {
        return typeCast == null
                ? operand.sql(metadata, dbVersion)
                : typeCastToSql(operand.sql(metadata, dbVersion), typeCast, metadata, dbVersion);
    }

    private void validateSelf() {
        // SQL Server requires that at least one of the expressions in the THEN or ELSE clauses isn't the NULL constant.
        // https://learn.microsoft.com/en-us/sql/t-sql/language-elements/case-transact-sql?view=sql-server-ver16#remarks
        // We adopt this requirement in general to avoid making implicit assumptions about the result type in such case.
        // Although, PostgreSQL, for example, would implicitly use the 'text' type.
        // Alternatively, we could receive hints about the expected type from outside of caseWhen, but this would require
        // significant changes to EQL internals, and there would still remain the ambiguous case of a top-level caseWhen
        // which can't take hints from the outside because there is no outside.

        if (typeCast == null) {
            final boolean hasNonNull = streamAllOperands().map(ISingleOperand3::type).anyMatch(PropType::isNotNull);
            if (!hasNonNull) {
                throw new EqlStage3ProcessingException("Illegal [caseWhen] expression: at least one returned value must be non-null or a type cast must be specified.");
            }
        }
    }

    private Stream<ISingleOperand3> streamAllOperands() {
        final Stream<ISingleOperand3> thens = whenThenPairs.stream().map(pair -> pair._2);
        return elseOperand == null ? thens : Stream.concat(thens, Stream.of(elseOperand));
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
        return this == obj
               || obj instanceof CaseWhen3 that
                  && Objects.equals(whenThenPairs, that.whenThenPairs)
                  && Objects.equals(elseOperand, that.elseOperand)
                  && Objects.equals(typeCast, that.typeCast)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("whenThenPairs", whenThenPairs)
                .addIfNotNull("else", elseOperand)
                .addIfNotNull("typeCast", typeCast);
    }

}
