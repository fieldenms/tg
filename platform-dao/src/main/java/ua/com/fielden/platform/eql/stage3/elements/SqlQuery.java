package ua.com.fielden.platform.eql.stage3.elements;

import java.util.HashMap;
import java.util.Map;

public class SqlQuery {
    public final Map<String, PropColumn> ys = new HashMap<>();
    public final TableAsQuerySource src1;
    public final TableAsQuerySource src2;
    public final SqlCondition cond;
    public final Long uid;
    
    public SqlQuery(final Long uid, final TableAsQuerySource src1, final TableAsQuerySource src2, final SqlCondition cond, final Map<String, PropColumn> ys) {
        this.uid = uid;
        this.src1 = src1;
        this.src2 = src2;
        this.cond = cond;
        this.ys.putAll(ys);
    }
    
    public String sql() {
        return "SELECT " + "*" + " AS C_1 " + " FROM " + src1.sql() + ", " + src2.sql() + " WHERE " + cond.sql(); 
    }
    
}
