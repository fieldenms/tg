package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class ChildGroup {
    final String name;
    final QrySource2BasedOnPersistentType source;
    final boolean required;
    final List<ChildGroup> items;
    
    final Map<String, String> paths;
    
    final Expression2 expr;
    
    public ChildGroup(final String name, final List<ChildGroup> items, final Map<String, String> paths, final boolean required, final QrySource2BasedOnPersistentType source, final Expression2 expr) {
        this.name = name;
        this.items = items;
        this.required = required;
        this.source = source;
        this.paths = paths;
        this.expr = expr;
//        assert(items.isEmpty() || !items.isEmpty() && source !=null );
//        assert(!items.isEmpty() || items.isEmpty() && !paths.isEmpty());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + items.hashCode();
        result = prime * result + paths.hashCode();
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
        
        return Objects.equals(name, other.name) && //
                Objects.equals(source, other.source) && //
                Objects.equals(required, other.required) && //
                Objects.equals(items, other.items) && //
                Objects.equals(paths, other.paths) && //
                Objects.equals(expr, other.expr);
    }
}