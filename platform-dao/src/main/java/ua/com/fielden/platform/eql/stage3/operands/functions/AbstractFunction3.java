package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.AbstractSingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public abstract class AbstractFunction3 extends AbstractSingleOperand3 {

    public AbstractFunction3(PropType type) {
        super(type);
    }

    @Override
    public String sql(final IDomainMetadata metadata) {
        throw new EqlStage3ProcessingException("Function [%s] is not yet implemented for RDBMS [%s]!".formatted(getClass().getSimpleName(), metadata.dbVersion()));
    }

}
