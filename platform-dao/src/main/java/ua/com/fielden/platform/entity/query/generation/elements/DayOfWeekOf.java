package ua.com.fielden.platform.entity.query.generation.elements;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class DayOfWeekOf extends SingleOperandFunction {

	public DayOfWeekOf(final ISingleOperand operand, final DbVersion dbVersion) {
		super(dbVersion, operand);
	}

	@Override
	public String sql() {
		switch (getDbVersion()) {
		case H2:
			return format("ISO_DAY_OF_WEEK(%s)", getOperand().sql());
		case MSSQL:
			return format("((DATEPART(DW, %s) + @@DATEFIRST - 1) %% 8 + (DATEPART(DW, %s) + @@DATEFIRST - 1) / 8)",
					getOperand().sql(), getOperand().sql());
		case POSTGRESQL:
			return format("CAST(EXTRACT(ISODOW FROM %s) AS INT)", getOperand().sql());
		default:
			throw new EqlException(format("Function [%s] is not yet implemented for RDBMS [%s]!",
					getClass().getSimpleName(), getDbVersion()));
		}
	}
}