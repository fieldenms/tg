package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.Limit;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.NO_OFFSET;

public class OrderBys3 {

    private final List<OrderBy3> models;
    private final Limit limit;
    private final long offset;

    public OrderBys3(final List<OrderBy3> models) {
        this(models, Limit.all(), NO_OFFSET);
    }

    public OrderBys3(final List<OrderBy3> models, final Limit limit, final long offset) {
        this.models = models;
        this.limit = limit;
        this.offset = offset;
    }

    public List<OrderBy3> getModels() {
        return unmodifiableList(models);
    }

    public long offset() {
        return offset;
    }

    public Limit limit() {
        return limit;
    }

    public String sql(final DbVersion dbVersion) {
        return models.stream().map(y -> y.sql(dbVersion)).collect(joining(", "));
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
