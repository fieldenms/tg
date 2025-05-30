package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

import static java.lang.String.format;

public class AddDateInterval3 extends TwoOperandsFunction3 {
    private final DateIntervalUnit intervalUnit;
    
    public AddDateInterval3(final ISingleOperand3 operand1, final DateIntervalUnit intervalUnit, final ISingleOperand3 operand2, final PropType type) {
        super(operand1, operand2, type);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        switch (dbVersion) {
        case POSTGRESQL:
            // Date operator needs to be explicitly typecasted to timestamp.
            // For more details, please refer to https://stackoverflow.com/questions/7475876/using-hibernate-query-colon-gets-treated-as-parameter-escaping-colon
            return format("(INTERVAL '1 %s' * %s + %s \\:\\:timestamp)", intervalUnit, operand1.sql(metadata, dbVersion), operand2.sql(metadata, dbVersion));
        case H2:
            return format("DATEADD('%s', %s, %s)", intervalUnit, operand1.sql(metadata, dbVersion), operand2.sql(metadata, dbVersion));
        case MSSQL:
            return format("DATEADD(%s, %s, %s)", intervalUnit, operand1.sql(metadata, dbVersion), operand2.sql(metadata, dbVersion));
        default:
            return super.sql(metadata, dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + intervalUnit.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AddDateInterval3 that
                  && Objects.equals(intervalUnit, that.intervalUnit)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("unit", intervalUnit);
    }

}
