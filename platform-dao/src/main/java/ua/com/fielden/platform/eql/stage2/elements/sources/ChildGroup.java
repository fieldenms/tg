package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.types.tuples.T2;

public class ChildGroup {
    final String mainName;
    final QrySource2BasedOnPersistentType source;
    final boolean required;
    final List<ChildGroup> items;
    
    final Set<T2<String, IQrySource2<?>>> paths;
    
    final Expression2 expr;
    
    public ChildGroup(final String mainName, final List<ChildGroup> items, final Set<T2<String, IQrySource2<?>>> paths, final boolean required, final QrySource2BasedOnPersistentType source, final Expression2 expr) {
        this.mainName = mainName;
        this.items = items;
        this.required = required;
        this.source = source;
        this.paths = paths;
        this.expr = expr;
        assert(items.isEmpty() || !items.isEmpty() && source !=null );
        assert(!items.isEmpty() || items.isEmpty() && !paths.isEmpty());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + items.hashCode();
        result = prime * result + mainName.hashCode();
        result = prime * result + paths.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChildGroup)) {
            return false;
        }

        final ChildGroup other = (ChildGroup) obj;
        
        return Objects.equals(mainName, other.mainName) && Objects.equals(items, other.items) && Objects.equals(paths, other.paths) && Objects.equals(source, other.source);
    }
}