package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;
import ua.com.fielden.platform.eql.stage3.INode3;

public interface IJoinNode3 extends IGenerateSql, INode3 {
    boolean needsParentheses();
}
