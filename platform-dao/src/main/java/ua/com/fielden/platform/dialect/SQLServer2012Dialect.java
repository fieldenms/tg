package ua.com.fielden.platform.dialect;

import org.hibernate.dialect.pagination.LegacyLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;

/**
 * An alternative SQL Server 2012 dialect that overrides the limit handler to make pagination possible with the current generation of EQL.
 * 
 * @author TG Team
 *
 */
public class SQLServer2012Dialect extends org.hibernate.dialect.SQLServer2012Dialect {

    @Override
    public LimitHandler buildLimitHandler(String sql, RowSelection selection) {
        // the original implementation was returning new SQLServer2005LimitHandler( sql, selection )
        // however, that implementation was not good enough for EQL constructed queries
        // hence, the fallback to the legacy limit handler
        return new LegacyLimitHandler(this, sql, selection);
    }

}