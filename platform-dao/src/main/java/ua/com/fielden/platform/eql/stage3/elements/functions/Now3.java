package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Now3 extends AbstractFunction3 {

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return "NOW()";
        case MSSQL:
            return "GETDATE()";
        case POSTGRESQL:
            return "CURRENT_TIMESTAMP";
        default:
            return super.sql(dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + Now3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Now3;
    } 
}
