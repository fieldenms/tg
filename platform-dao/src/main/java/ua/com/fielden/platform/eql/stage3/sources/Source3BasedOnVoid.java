package ua.com.fielden.platform.eql.stage3.sources;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;

public class Source3BasedOnVoid implements ISource3 {
    
    @Override
    public String column(final String colName) {
         throw new EqlStage3ProcessingException("This method shouldn't be invoked.");
    }

    @Override
    public String sqlAlias() {
        throw new EqlStage3ProcessingException("This method shouldn't be invoked.");
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
        return prime * result + Source3BasedOnVoid.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Source3BasedOnVoid;
    } 
}