package ua.com.fielden.platform.eql.stage3.elements.operands;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface ISetOperand3 {
    String sql(final DbVersion dbVersion);
}
