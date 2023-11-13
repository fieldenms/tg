package ua.com.fielden.platform.eql.stage1.sundries;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;

public class OrderBys1 {
    public static final OrderBys1 EMPTY_ORDER_BYS = new OrderBys1(emptyList());
    
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
        this.models = models;
    }

    public OrderBys2 transform(final TransformationContextFromStage1To2 context) {
        if (models.isEmpty()) {
            return OrderBys2.EMPTY_ORDER_BYS;
        } else {
            return new OrderBys2(models.stream().map(el -> el.transform(context)).collect(toList()));
        }
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return models.isEmpty() ? emptySet() : models.stream().filter(el -> el.operand != null).map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
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

        if (!(obj instanceof OrderBys1)) {
            return false;
        }

        final OrderBys1 other = (OrderBys1) obj;

        return Objects.equals(models, other.models);
    }
}