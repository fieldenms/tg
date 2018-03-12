package ua.com.fielden.platform.entity.query.generation.elements;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;

public class AddDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public AddDateInterval(final ISingleOperand intervalValue, final DateIntervalUnit intervalUnit, final ISingleOperand date, final DbVersion dbVersion) {
        super(dbVersion, intervalValue, date);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql() {
        switch (getDbVersion()) {
        case H2:
            return format("DATEADD('%s', %s, %s)",  intervalUnit, getOperand1().sql(), getOperand2().sql());
        case MSSQL:
            return format("DATEADD(%s, %s, %s)",  intervalUnit, getOperand1().sql(), getOperand2().sql());
        default:
            throw new EqlException(format("Function [%s] is not yet implemented for RDBMS [%s]!", getClass().getSimpleName(), getDbVersion()));
        }
    }
}