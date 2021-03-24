package ua.com.fielden.platform.eql.stage3.sources;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.core.Yield3;
import ua.com.fielden.platform.eql.stage3.operands.SourceQuery3;

public class QrySource3BasedOnSubqueries implements IQrySource3 {
    private final List<SourceQuery3> models = new ArrayList<>();
    public final String id;
    public final int sqlId;
    private final Map<String, String> columns = new HashMap<>(); //keys can contain strings with dots
    
    public QrySource3BasedOnSubqueries(final List<SourceQuery3> models, final String id, final int sqlId) {
        this.models.addAll(models);
        this.id = id;
        this.sqlId = sqlId;
        for (final Yield3 entry : models.get(0).yields.getYields()) {
            if (!entry.isHeader) {
                columns.put(entry.alias, entry.column);    
            }
        }
    }

    @Override
    public String column(final String colName) {
         return columns.get(colName);
    }

    @Override
    public String sqlAlias() {
        return "Q_" + sqlId;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return "(" + models.stream().map(m -> m.sql(dbVersion)).collect(joining("\n UNION ALL")) + ") AS " + sqlAlias();
    }

    @Override
    public String toString() {
        return "QrySource3BasedOnSubqueries of type " + models.get(0).resultType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + models.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof QrySource3BasedOnSubqueries)) {
            return false;
        }
        
        final QrySource3BasedOnSubqueries other = (QrySource3BasedOnSubqueries) obj;
        
        return Objects.equals(models, other.models) && Objects.equals(id, other.id);
    }
}