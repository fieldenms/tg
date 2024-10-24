package ua.com.fielden.platform.eql.stage3.sources;

import static java.lang.String.format;

import java.util.Map;
import java.util.Objects;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;

public abstract class AbstractSource3 implements ISource3 {
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
            throw new EqlStage3ProcessingException(format("Query source doesn't contain column for property [%s]", propName));
        }
    }

    private String searchForColumn(final String propName) {
        final String column = columns.get(propName);

        if (column != null) {
            return column;
        }

        // Quick solution that allows to skip adding ".amount" for Money typed yields aliases.
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
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractSource3)) {
            return false;
        }

        final AbstractSource3 other = (AbstractSource3) obj;

        return
                Objects.equals(sqlAlias, other.sqlAlias) &&
                Objects.equals(id, other.id) && Objects.equals(columns, other.columns);
    }
}
