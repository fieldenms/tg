package ua.com.fielden.platform.eql.stage3.sundries;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.NO_OFFSET;
import static ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3.isSubQuery;

public class OrderBys3 {

    private final List<OrderBy3> models;
    private final Limit limit;
    private final long offset;

    public OrderBys3(final List<OrderBy3> models) {
        this(models, Limit.all(), NO_OFFSET);
    }

    public OrderBys3(final List<OrderBy3> models, final Limit limit, final long offset) {
        this.models = ImmutableList.copyOf(models);
        this.limit = limit;
        this.offset = offset;
    }

    public List<OrderBy3> getModels() {
        return models;
    }

    public long offset() {
        return offset;
    }

    public Limit limit() {
        return limit;
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final AbstractQuery3 enclosingQuery) {
        final var modelsStr = models.stream().map(o -> o.sql(metadata, dbVersion)).collect(joining(", "));

        final var sb = new StringBuilder();

        switch (dbVersion) {
            // PostgreSQL supports shorter syntax (MySQL too)
            case POSTGRESQL, MYSQL -> {
                sb.append(modelsStr);
                if (limit instanceof Limit.Count(long count)) {
                    sb.append(" LIMIT ").append(count).append(' ');
                }
                if (offset != NO_OFFSET) {
                    sb.append(" OFFSET ").append(offset).append(' ');
                }
            }
            case MSSQL -> {
                sb.append(modelsStr);
                // 1. limit (FETCH) can only appear after OFFSET, so we need offset if we have limit
                // 2. If this is a subquery, then OFFSET must be specified, even if it's zero.
                // SQL Server will reject ORDER BY in a subquery without OFFSET or TOP.
                if (offset != NO_OFFSET || limit instanceof Limit.Count || isSubQuery(enclosingQuery)) {
                    sb.append(" OFFSET ").append(offset).append(" ROWS ");
                    if (limit instanceof Limit.Count(long count)) {
                        sb.append(" FETCH FIRST ").append(count).append(" ROWS ONLY ");
                    }
                }
            }
            // OFFSET and FETCH FIRST from SQL standard
            // Supported by: MSSQL
            default -> {
                sb.append(modelsStr);
                // limit (FETCH) can only appear after OFFSET, so we need offset if we have limit
                if (offset != NO_OFFSET || limit instanceof Limit.Count) {
                    sb.append(" OFFSET ").append(offset).append(" ROWS ");
                }
                if (limit instanceof Limit.Count(long count)) {
                    sb.append(" FETCH FIRST ").append(count).append(" ROWS ONLY ");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(models, limit, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof OrderBys3 that
                              && offset == that.offset
                              && limit.equals(that.limit)
                              && models.equals(that.models);
    }

}
