package ua.com.fielden.platform.eql.stage3.operands;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;

public class Expression3 extends AbstractSingleOperand3 {

    public final ISingleOperand3 first;
    private final List<CompoundSingleOperand3> items;

    public Expression3(final ISingleOperand3 first, final List<CompoundSingleOperand3> items, final PropType type) {
        super(type);
        this.first = first;
        this.items = items;
    }

    public boolean isSingle() {
        return items.isEmpty();
    }
    
    @Override
    public String sql(final DbVersion dbVersion) {
        return items.isEmpty() ? first.sql(dbVersion) : "(" + first.sql(dbVersion) + items.stream().map(co -> co.sql(dbVersion)).collect(joining()) +")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + first.hashCode();
        result = prime * result + items.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
 
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof Expression3)) {
            return false;
        }
        
        final Expression3 other = (Expression3) obj;
        
        return Objects.equals(first, other.first) && Objects.equals(items, other.items);
    }
}