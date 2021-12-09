package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;

public interface ISources3 extends IGenerateSql {
    boolean needsParentheses();
}
