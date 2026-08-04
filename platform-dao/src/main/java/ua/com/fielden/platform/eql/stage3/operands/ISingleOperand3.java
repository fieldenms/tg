package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.IGenerateSql;
import ua.com.fielden.platform.eql.stage3.INode3;

public interface ISingleOperand3 extends IGenerateSql, INode3 {
    PropType type();
}
