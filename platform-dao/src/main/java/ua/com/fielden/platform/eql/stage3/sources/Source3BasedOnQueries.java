package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;

import java.util.*;

import static java.util.stream.Collectors.joining;

public class Source3BasedOnQueries extends AbstractSource3 {
    private final List<SourceQuery3> models = new ArrayList<>();
    
    public Source3BasedOnQueries(final List<SourceQuery3> models, final Integer id, final int sqlId) {
        super("Q_" + sqlId, id, obtainColumnsFromYields(models.get(0).yields.getYields()));
        this.models.addAll(models);
    }
    
    private static Map<String, String> obtainColumnsFromYields(final Collection<Yield3> yields) {
        final Map<String, String> result = new HashMap<>();
        for (final Yield3 entry : yields) {
            result.put(entry.alias, entry.column);    
        }
        return result;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return switch (dbVersion) {
            // a SELECT with an ORDER BY inside a UNION must be enclosed in parentheses, although this inner ordering
            // is not guaranteed to have an effect on the results of UNION
            // https://www.postgresql.org/docs/16/sql-select.html#SQL-UNION
            case POSTGRESQL ->
                    "("
                    + models.stream().map(m -> '(' + m.sql(dbVersion) + ')').collect(joining("\n UNION ALL \n"))
                    + ") AS " + sqlAlias;
            default ->
                    "("
                    + models.stream().map(m -> m.sql(dbVersion)).collect(joining("\n UNION ALL \n"))
                    + ") AS " + sqlAlias;
        };
    }

    @Override
    public String toString() {
        return "Source3BasedOnQueries of type " + models.get(0).resultType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + models.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof Source3BasedOnQueries)) {
            return false;
        }
        
        final Source3BasedOnQueries other = (Source3BasedOnQueries) obj;
        
        return Objects.equals(models, other.models);
    }
}
