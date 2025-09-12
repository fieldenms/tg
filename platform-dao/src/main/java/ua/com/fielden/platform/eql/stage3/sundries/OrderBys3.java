package ua.com.fielden.platform.eql.stage3.sundries;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.NO_OFFSET;
import static ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3.isSubQuery;
import static ua.com.fielden.platform.eql.stage3.sundries.OrderBy3.ASC;
import static ua.com.fielden.platform.eql.stage3.sundries.OrderBy3.DESC;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;

public record OrderBys3 (List<OrderBy3> list, Limit limit, long offset) implements ToString.IFormattable {

    private static final String LIMIT = " LIMIT ";
    private static final String OFFSET = " OFFSET ";
    private static final String ROWS = " ROWS ";
    private static final String FETCH_FIRST = " FETCH FIRST ";
    private static final String ROWS_ONLY = " ROWS ONLY ";

    public OrderBys3(final List<OrderBy3> list) {
        this(list, Limit.all(), NO_OFFSET);
    }

    public OrderBys3(final List<OrderBy3> list, final Limit limit, final long offset) {
        this.list = ImmutableList.copyOf(list);
        this.limit = limit;
        this.offset = offset;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final AbstractQuery3 enclosingQuery) {
        // Deduplicate the order-by list to ensure compatibility with SQL Server.
        // We do this by comparing the SQL for each item.
        // This is by far the simplest solution.
        // Its drawbacks are:
        // 1. Inability to test the deduplication by EQL AST comparison.
        // 2. Expressions with side-effects are not respected.
        //    Currently, EQL does not support any expressions with side-effects (e.g., a function that generates a random number),
        //    so this is not a problem.
        //
        // An alternative approach is to consider each item as an AST node, and compare their types and structure,
        // which requires a lot of additional complexity for a thorough solution (a comparison for each type of node).
        //
        // There is one rare case that is not taken into account here.
        // SQL Server requires an expression from the select list, and its alias cannot be present in an order-by list at the same time.
        // E.g., `SELECT name AS c FROM t ORDER BY name, c` is an invalid query.
        //
        // See Issue #2429.
        final var listSql = distinct(list.stream()
                                         .map(m -> t2(m.mapExpression(operand -> operand.sql(metadata, dbVersion), Yield3::column),
                                                      m.isDesc())),
                                     t2 -> t2._1)
                .map(t2 -> t2.map((sql, desc) -> sql + (desc ? DESC : ASC)))
                .collect(joining(", "));

        final var sb = new StringBuilder();

        switch (dbVersion) {
            // PostgreSQL supports shorter syntax (MySQL too)
            case POSTGRESQL, MYSQL -> {
                sb.append(listSql);
                if (limit instanceof Limit.Count(long count)) {
                    sb.append(LIMIT).append(count).append(' ');
                }
                if (offset != NO_OFFSET) {
                    sb.append(OFFSET).append(offset).append(' ');
                }
            }
            case MSSQL -> {
                sb.append(listSql);
                // 1. Limit (FETCH) can only appear after OFFSET, so there needs to be OFFSET if there is FETCH.
                // 2. If this is a subquery, then OFFSET must be specified, even if it is zero.
                // SQL Server will reject ORDER BY in a subquery without OFFSET or TOP.
                if (offset != NO_OFFSET || limit instanceof Limit.Count || isSubQuery(enclosingQuery)) {
                    sb.append(OFFSET).append(offset).append(ROWS);
                    if (limit instanceof Limit.Count(long count)) {
                        sb.append(FETCH_FIRST).append(count).append(ROWS_ONLY);
                    }
                }
            }
            // OFFSET and FETCH FIRST from SQL standard
            // Supported by: MSSQL
            default -> {
                sb.append(listSql);
                // limit (FETCH) can only appear after OFFSET, so we need offset if we have a limit
                if (offset != NO_OFFSET || limit instanceof Limit.Count) {
                    sb.append(OFFSET).append(offset).append(ROWS);
                }
                if (limit instanceof Limit.Count(long count)) {
                    sb.append(FETCH_FIRST).append(count).append(ROWS_ONLY);
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("limit", limit)
                .add("offset", offset)
                .add("list", list)
                .$();
    }

}
