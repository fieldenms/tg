package ua.com.fielden.platform.eql.stage3.functions;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.entity.query.DbVersion;

public class CountAll3 extends AbstractFunction3 {
    
    public static CountAll3 INSTANCE = new CountAll3();
    
    private CountAll3() {
        super(Integer.class, IntegerType.INSTANCE);
    }

    private static final String sql = "COUNT(*)";
    
    @Override
    public String sql(final DbVersion dbVersion) {
        return sql;
    }
}