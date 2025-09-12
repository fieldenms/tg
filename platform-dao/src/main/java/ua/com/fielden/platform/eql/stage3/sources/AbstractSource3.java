package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractSource3 implements ISource3, ToString.IFormattable {
    public final String sqlAlias;
    private final Integer id;
    private final Map<String, String> columns;
    private static final String MONEY_TYPE_SUBPROP_ENDING = ".amount";
    private static final int MONEY_TYPE_SUBPROP_ENDING_LENGTH = MONEY_TYPE_SUBPROP_ENDING.length();

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
        final String column = searchForColumn(propName);
        if (column != null) {
            return sqlAlias + "." + column;
        } else {
            throw new EqlStage3ProcessingException("Query source doesn't contain column for property [%s]".formatted(propName));
        }
    }

    /**
     * This method contains a workaround that enables shortcuts in yield aliases used in {@linkplain SourceQuery3 source queries}:
     * <ul>
     *   <li> {@code money} instead of {@code money.amount}
     * </ul>
     * This is only useful for {@link Source3BasedOnQueries}, because {@link Source3BasedOnTable} has its {@link #columns}
     * populated from {@link EqlTable} which provides column names for all component sub-properties.
     * Effectively, this enables a source query to use a component-typed property as an alias directly, while supporting
     * correct property resolution in its enclosing query.
     * For example, when a source query with a {@link Source3BasedOnQueries} as its source has a yield with alias {@code money},
     * its {@link #columns} will not contain Money sub-property ({@code money.amount}).
     * At the same time, the enclosing query may use {@code prop("money")} that will be expanded to {@code prop("money.amount")} by EQL
     * (given that the source query is based on an entity type with property {@code money : Money}).
     * Without this workaround, the resolution of the expanded property will fail.
     * <p>
     * Ideally, {@link Source3BasedOnQueries} should be smarter about populating its {@link #columns} from yields, leveraging
     * {@link Source2BasedOnQueries#querySourceInfo()} to identify component-typed properties used as yield aliases.
     */
    private String searchForColumn(final String propName) {
        final String column = columns.get(propName);

        if (column != null) {
            return column;
        }

        // The workaround
        if (propName.endsWith(MONEY_TYPE_SUBPROP_ENDING)) {
            return columns.get(propName.substring(0, propName.length() - MONEY_TYPE_SUBPROP_ENDING_LENGTH));
        }

        return null;
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
