package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;

public class AddDateInterval extends TwoOperandsFunction1 {

    public AddDateInterval(final ISingleOperand1 operand1, final ISingleOperand1 operand2) {
        super(operand1, operand2);
        // TODO Auto-generated constructor stub
    }

    @Override
    public TransformationResult<IIgnorableAtS2> transform(final PropsResolutionContext resolutionContext) {
        // TODO Auto-generated method stub
        return null;
    }

//    private DateIntervalUnit intervalUnit;
//
//    public AddDateInterval(final ISingleOperand intervalValue, final DateIntervalUnit intervalUnit, final ISingleOperand date, final DbVersion dbVersion) {
//        super(dbVersion, intervalValue, date);
//        this.intervalUnit = intervalUnit;
//    }
//
//    @Override
//    public String sql() {
//        switch (getDbVersion()) {
//        case POSTGRESQL:
//            return format("('1 %s' * %s + %s)",  intervalUnit, getOperand1().sql(), getOperand2().sql());
//        case H2:
//            return format("DATEADD('%s', %s, %s)",  intervalUnit, getOperand1().sql(), getOperand2().sql());
//        case MSSQL:
//            return format("DATEADD(%s, %s, %s)",  intervalUnit, getOperand1().sql(), getOperand2().sql());
//        default:
//            throw new EqlException(format("Function [%s] is not yet implemented for RDBMS [%s]!", getClass().getSimpleName(), getDbVersion()));
//        }
//    }
}