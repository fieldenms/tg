package ua.com.fielden.platform.eql.stage3.operands;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Expression3 implements ISingleOperand3 {

    public final ISingleOperand3 first;
    private final List<CompoundSingleOperand3> items;

    public Expression3(final ISingleOperand3 first, final List<CompoundSingleOperand3> items) {
        this.first = first;
        this.items = items;
    }

    public boolean isSingle() {
        return items.isEmpty();
    }
    
    @Override
    public Class<?> type() {
        // TODO EQL
        final Set<Class<?>> types = new HashSet<>();
        types.add(first.type());
        for (final CompoundSingleOperand3 item : items) {
            types.add(item.operand.type());
        }
        
        return types.iterator().next();
    }

    @Override
    public Object hibType() {
        // TODO EQL
        final Set<Object> hibTypes = new HashSet<>();
        hibTypes.add(first.hibType());
        for (final CompoundSingleOperand3 item : items) {
            hibTypes.add(item.operand.hibType());
        }
        
        return hibTypes.iterator().next();
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return items.isEmpty() ? first.sql(dbVersion) : "(" + first.sql(dbVersion) + items.stream().map(co -> co.sql(dbVersion)).collect(joining()) +")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + items.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Expression3)) {
            return false;
        }
        
        final Expression3 other = (Expression3) obj;
        
        return Objects.equals(first, other.first) && Objects.equals(items, other.items);
    }
}