package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class OrderBys3 {
    private final List<OrderBy3> models;

    public OrderBys3(final List<OrderBy3> models) {
        this.models = models;
    }

    public List<OrderBy3> getModels() {
        return unmodifiableList(models);
    }

    public String sql(final IDomainMetadata metadata) {
        return models.stream().map(o -> o.sql(metadata)).collect(joining(", "));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + models.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBys3)) {
            return false;
        }

        final OrderBys3 other = (OrderBys3) obj;

        return Objects.equals(models, other.models);
    }

}
