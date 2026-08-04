package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;
import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public class UpperCaseOf3 extends SingleOperandFunction3 {

    public UpperCaseOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public UpperCaseOf3 setOperand(final ISingleOperand3 operand) {
        return operand == this.operand ? this : new UpperCaseOf3(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return format("UPPER(%s)", operandToSqlAsString(metadata, dbVersion, operand));
    }
}
