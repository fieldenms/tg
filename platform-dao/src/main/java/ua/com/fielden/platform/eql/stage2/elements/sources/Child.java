package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class Child implements Comparable<Child> {
    final AbstractPropInfo<?> main;
    final Expression2 expr;
    final boolean required;
    final Set<Child> items;
    final String fullPath; //not null if given child represents explicit prop that needs resolution 
    final String context; //indicates context for table being joined within main (explicit) table (aka dot.notation being resolved by joining this table)
    final QrySource2BasedOnPersistentType source;
    final IQrySource2<?> parentSource;
    
    public Child(final AbstractPropInfo<?> main, final Set<Child> items, final String fullPath, final String context, final boolean required, final QrySource2BasedOnPersistentType source) {
        this(main, items, fullPath, context, required, source, null, null);
    }
    
    public Child(final AbstractPropInfo<?> main, final Set<Child> items, final String fullPath, final String context, final boolean required, final QrySource2BasedOnPersistentType source, final Expression2 expr, final IQrySource2<?> parentSource) {
        this.main = main;
        this.items = items;
        this.fullPath = fullPath;
        this.context = context;
        this.required = required;
        this.source = source;
        this.parentSource = parentSource;
        this.expr = expr;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + items.hashCode();
        result = prime * result + main.hashCode();
        result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((parentSource == null) ? 0 : parentSource.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Child)) {
            return false;
        }

        final Child other = (Child) obj;
        
        return Objects.equals(main, other.main) && Objects.equals(items, other.items) && Objects.equals(fullPath, other.fullPath) && Objects.equals(source, other.source) && Objects.equals(parentSource, other.parentSource);
    }

    @Override
    public int compareTo(final Child o) {
        return context.compareTo(o.context);
    }
}