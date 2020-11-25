package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface ISingleOperand3 {
    String sql(final DbVersion dbVersion);
    
    default Class<?> type() {return null;}
    default Object hibType() {return null;}
}
