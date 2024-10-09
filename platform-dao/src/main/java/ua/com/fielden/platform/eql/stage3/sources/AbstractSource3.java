package ua.com.fielden.platform.eql.stage3.sources;

import static java.lang.String.format;

import java.util.Map;
import java.util.Objects;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.types.RichText;

public abstract class AbstractSource3 implements ISource3 {
    public final String sqlAlias;
    private final Integer id;
    private final Map<String, String> columns;
    private static final String MONEY_TYPE_SUBPROP_ENDING = ".amount";
    private static final int MONEY_TYPE_SUBPROP_ENDING_LENGTH = MONEY_TYPE_SUBPROP_ENDING.length();
    private static final String RICHTEXT_CORE_TEXT_SUFFIX = '.' + RichText._coreText;
    private static final int RICHTEXT_CORE_TEXT_SUFFIX_LENGTH = RICHTEXT_CORE_TEXT_SUFFIX.length();

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

    /**
     * This method contains a workaround that enables shortcuts in yield aliases used in {@linkplain SourceQuery3 source queries}:
     * <ul>
     *   <li> {@code money} instead of {@code money.amount}
     *   <li> {@code richText} instead of {@code richText.coreText}.
     * </ul>
     * This is only useful for {@link Source3BasedOnQueries}, because {@link Source3BasedOnTable} has its {@link #columns}
     * populated from {@link EqlTable} which provides column names for all component sub-properties.
     * Effectively, this enables a source query to use a component-typed property as an alias directly, while supporting
     * correct property resolution in its enclosing query.
     * For example, when a source query with a {@link Source3BasedOnQueries} as its source has a yield with alias {@code richText},
     * its {@link #columns} will not contain component sub-properties ({@code richText.coreText} & {@code richText.formattedText}).
     * At the same time, the enclosing query may use {@code prop("richText")} that will be expanded to {@code prop("richText.coreText")}
     * (given that the source query is based on an entity type with property {@code richText : RichText}).
     * Without this workaround, the resolution of the expanded property will fail.
     * <p>
     * Ideally, {@link Source3BasedOnQueries} should be smarter about populating its {@link #columns} from yields, leveraging
     * {@link Source2BasedOnQueries#querySourceInfo()} to identify component-typed properties used as yield aliases.
     *
     * @see ua.com.fielden.platform.entity.query.fetching.EqlRichTextTest#RichText_property_used_as_yield_alias_in_source_query_can_be_resolved_in_top_level_query()
     */
    private String searchForColumn(final String propName) {
        final String column = columns.get(propName);

        if (column != null) {
            return column;
        }

        // The workaround
        if (propName.endsWith(MONEY_TYPE_SUBPROP_ENDING)) {
            return columns.get(propName.substring(0, propName.length() - MONEY_TYPE_SUBPROP_ENDING_LENGTH));
        } else if (propName.endsWith(RICHTEXT_CORE_TEXT_SUFFIX)) {
            return columns.get(propName.substring(0, propName.length() - RICHTEXT_CORE_TEXT_SUFFIX_LENGTH));
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
