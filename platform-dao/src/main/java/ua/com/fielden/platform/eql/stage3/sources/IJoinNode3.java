package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;

public interface IJoinNode3 extends IGenerateSql {
    boolean needsParentheses();
}
