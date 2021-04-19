package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;

public interface ISingleOperand3 extends IGenerateSql {
    Class<?> type();
    Object hibType();
}
