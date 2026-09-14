package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.utils.ToString;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractSource3 implements ISource3, ToString.IFormattable {
    public final String sqlAlias;
    private final Integer id;
    private final Map<String, String> columns;

    protected AbstractSource3(final String sqlAlias, final Integer id, final Map<String, String> columns) {
        this.sqlAlias = sqlAlias;
        this.id = id;
        this.columns = Map.copyOf(columns);
    }

    @Override
    public Integer id() {
        return id;
    }

    @Override
    public String column(final String propName) {
        final String column = columns.get(propName);
        if (column != null) {
            return sqlAlias + "." + column;
        } else {
            throw new EqlStage3ProcessingException("Query source doesn't contain column for property [%s]".formatted(propName));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + sqlAlias.hashCode();
        result = prime * result + columns.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractSource3 that
                  && Objects.equals(sqlAlias, that.sqlAlias)
                  && Objects.equals(id, that.id)
                  && Objects.equals(columns, that.columns);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("id", id)
                .addIfNotNull("sqlAlias", sqlAlias)
                .add("columns", columns)
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
