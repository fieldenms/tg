package ua.com.fielden.platform.eql.stage3.elements;

import java.util.List;
import java.util.Objects;

public class OrderBys3 {
    private final List<OrderBy3> models;

    public OrderBys3(final List<OrderBy3> models) {
        this.models = models;
    }

    public List<OrderBy3> getModels() {
        return models;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((models == null) ? 0 : models.hashCode());
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