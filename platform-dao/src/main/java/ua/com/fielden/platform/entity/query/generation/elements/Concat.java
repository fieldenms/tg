package ua.com.fielden.platform.entity.query.generation.elements;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.type.StringType;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Concat extends AbstractFunction implements ISingleOperand {

    private final List<ISingleOperand> operands;

    public Concat(final List<ISingleOperand> operands, final DbVersion dbVersion) {
        super(dbVersion);
        this.operands = operands;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        final List<EntQuery> result = new ArrayList<EntQuery>();
        for (final ISingleOperand operand : operands) {
            result.addAll(operand.getLocalSubQueries());
        }
        return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
        final List<EntProp> result = new ArrayList<EntProp>();
        for (final ISingleOperand operand : operands) {
            result.addAll(operand.getLocalProps());
        }
        return result;
    }

    @Override
    public List<EntValue> getAllValues() {
        final List<EntValue> result = new ArrayList<EntValue>();
        for (final ISingleOperand operand : operands) {
            result.addAll(operand.getAllValues());
        }
        return result;
    }

    @Override
    public Class<?> type() {
        return String.class;
    }

    @Override
    public Object hibType() {
        return StringType.INSTANCE;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    public String sqlForH2AndMssql() {
        return "CONCAT (" + operands.stream().map(so -> getConvertToStringSql(so)).collect(joining(", ")) + ")";
    }

    public String sqlForPostgresql() {
        return " (" + operands.stream().map(so -> getConvertToStringSql(so)).collect(joining(" || ")) + ")";
    }

    @Override
    public String sql() {
        switch (getDbVersion()) {
        case H2:
        case MSSQL:
            return sqlForH2AndMssql();
        case POSTGRESQL:
            return sqlForPostgresql();
        default:
            throw new IllegalStateException("Function [" + getClass().getSimpleName() + "] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
        }
    }

    public List<ISingleOperand> getOperands() {
        return operands;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operands == null) ? 0 : operands.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Concat)) {
            return false;
        }
        final Concat other = (Concat) obj;
        if (operands == null) {
            if (other.operands != null) {
                return false;
            }
        } else if (!operands.equals(other.operands)) {
            return false;
        }
        return true;
    }
}