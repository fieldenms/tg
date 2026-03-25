package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class ConcatOf3 extends TwoOperandsFunction3 {

    public final List<ConcatOfOrderItem3> orderItems;

    public ConcatOf3(
            final ISingleOperand3 operand1,
            final ISingleOperand3 operand2,
            final PropType type,
            final List<ConcatOfOrderItem3> orderItems)
    {
        super(operand1, operand2, type);
        this.orderItems = List.copyOf(orderItems);
    }

    public ConcatOf3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        this(operand1, operand2, type, List.of());
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String exprSql = operand1.sql(metadata, dbVersion);
        final String sepSql = operand2.sql(metadata, dbVersion);

        if (orderItems.isEmpty()) {
            return format("STRING_AGG(%s, %s)", exprSql, sepSql);
        }

        final String orderBySql = orderItems.stream()
                .map(item -> item.sql(metadata, dbVersion))
                .collect(joining(", "));

        return switch (dbVersion) {
            case MSSQL -> format("STRING_AGG(%s, %s) WITHIN GROUP (ORDER BY %s)", exprSql, sepSql, orderBySql);
            default -> format("STRING_AGG(%s, %s ORDER BY %s)", exprSql, sepSql, orderBySql);
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ConcatOf3.class.getName().hashCode();
        result = prime * result + orderItems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof ConcatOf3 that
                  && super.equals(that)
                  && Objects.equals(orderItems, that.orderItems);
    }

}
