package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;

public class DayOfWeekOf extends SingleOperandFunction1 {

    public DayOfWeekOf(ISingleOperand1 operand) {
        super(operand);
        // TODO Auto-generated constructor stub
    }

    @Override
    public TransformationResult<IIgnorableAtS2> transform(PropsResolutionContext resolutionContext) {
        // TODO Auto-generated method stub
        return null;
    }

//	public DayOfWeekOf(final ISingleOperand operand, final DbVersion dbVersion) {
//		super(dbVersion, operand);
//	}
//
//	@Override
//	public String sql() {
//		switch (getDbVersion()) {
//		case H2:
//			return format("ISO_DAY_OF_WEEK(%s)", getOperand().sql());
//		case MSSQL:
//			return format("((DATEPART(DW, %s) + @@DATEFIRST - 1) %% 8 + (DATEPART(DW, %s) + @@DATEFIRST - 1) / 8)",
//					getOperand().sql(), getOperand().sql());
//		case POSTGRESQL:
//			return format("CAST(EXTRACT(ISODOW FROM %s) AS INT)", getOperand().sql());
//		default:
//			throw new EqlException(format("Function [%s] is not yet implemented for RDBMS [%s]!",
//					getClass().getSimpleName(), getDbVersion()));
//		}
//	}
}