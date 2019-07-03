package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.entity.query.DbVersion;

public class CountAll3 extends AbstractFunction3 {
    private static final String sql = "COUNT(*)";
    
    @Override
    public String sql(final DbVersion dbVersion) {
        return sql;
    }
   
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + CountAll3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof CountAll3;
    } 
}
