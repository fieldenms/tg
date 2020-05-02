package ua.com.fielden.platform.eql.stage3.elements.sources;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.stage3.elements.Column;

public class QrySource3BasedOnVoid implements IQrySource3 {
    
    @Override
    public Column column(final String colName) {
         throw new EqlException("This method shouldn't be invoked.");
    }

    @Override
    public String sqlAlias() {
        throw new EqlException("This method shouldn't be invoked.");
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return dbVersion == ORACLE ? "DUAL" : "";
    }

    @Override
    public String toString() {
        return "QrySource3BasedOnVoid";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + QrySource3BasedOnVoid.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof QrySource3BasedOnVoid;
    } 
}