package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public class Concat3 extends AbstractFunction3 {

    /// Non-empty list of operands.
    ///
    public final List<ISingleOperand3> operands;

    public Concat3(final List<ISingleOperand3> operands, final PropType type) {
        super(type);
        if (operands.isEmpty()) {
            throw new EqlStage3ProcessingException("There must be at least one operand.");
        }
        this.operands = operands;
    }

    public List<ISingleOperand3> operands() {
        return operands;
    }

    public Concat3 setOperands(final List<ISingleOperand3> operands) {
        return operands == this.operands ? this : new Concat3(operands, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
        case MSSQL:
            return " (%s)".formatted(operands.stream().map(so -> operandToSqlAsString(metadata, dbVersion, so)).collect(joining(" + ")));
        case POSTGRESQL:
            return " (%s)".formatted(operands.stream().map(so -> operandToSqlAsString(metadata, dbVersion, so)).collect(joining(" || ")));
        default:
            return super.sql(metadata, dbVersion);
        }
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("operands", operands);
    }

}
