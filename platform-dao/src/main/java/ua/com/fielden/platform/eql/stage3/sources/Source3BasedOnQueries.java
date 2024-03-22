package ua.com.fielden.platform.eql.stage3.sources;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;

import java.util.*;

import static java.util.stream.Collectors.joining;

public class Source3BasedOnQueries extends AbstractSource3 {
    private final List<SourceQuery3> models;
    
    public Source3BasedOnQueries(final List<SourceQuery3> models, final Integer id, final int sqlId) {
        super("Q_" + sqlId, id, obtainColumnsFromYields(models.getFirst().yields.getYields()));
        this.models = ImmutableList.copyOf(models);
    }
    
    private static Map<String, String> obtainColumnsFromYields(final Collection<Yield3> yields) {
        final Map<String, String> result = new HashMap<>();
        for (final Yield3 entry : yields) {
            result.put(entry.alias, entry.column);    
        }
        return result;
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        return "(" + models.stream().map(m -> m.sql(metadata)).collect(joining("\n UNION ALL \n")) + ") AS " + sqlAlias;
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
