package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.lang.String.format;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class Child implements Comparable<Child> {
    public final AbstractPropInfo<?> main;
    public final QrySource2BasedOnPersistentType source;
    public final boolean required;
    public final SortedSet<Child> items;
    
    public final String fullPath; //not null if given child represents explicit prop that needs resolution 
    public final IQrySource2<?> parentSource;

    public final Expression2 expr;
    public final Set<Child> dependencies;

    final int id;
    
    public Child(final AbstractPropInfo<?> main, final SortedSet<Child> items, final String fullPath, final boolean required, final QrySource2BasedOnPersistentType source, final Expression2 expr, final IQrySource2<?> parentSource, final Set<Child> dependencies, final int id) {
        this.main = main;
        this.items = items;
        this.fullPath = fullPath;
        this.required = required;
        this.source = source;
        this.parentSource = parentSource;
        this.expr = expr;
        this.dependencies = dependencies;
        this.id = id;
 //       assert(items.isEmpty() || !items.isEmpty() && source !=null );
        assert(dependencies.isEmpty() || !dependencies.isEmpty() && expr != null);
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
        result = prime * result + dependencies.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return format("%3s| %30s | fp=%25s | %50s | %50s |", id, main, /*context, */fullPath, parentSource, (source != null ? source : "none"));
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
        
        return Objects.equals(main, other.main) && Objects.equals(items, other.items) && Objects.equals(fullPath, other.fullPath) && Objects.equals(source, other.source) && Objects.equals(parentSource, other.parentSource) && Objects.equals(dependencies, other.dependencies);
    }
    
    private boolean dependsOn(final Child child) {
        return dependencies.contains(child) || dependencies.stream().anyMatch(c -> c.dependsOn(child));
    }
    
    @Override
    public int compareTo(final Child o) {
        return dependsOn(o) ? 1 : (o.dependsOn(this) ? -1 : main.name.equals(o.main.name) ? (id > o.id ? 1 : -1) : (expr != null && o.expr != null || expr == null && o.expr == null ? main.name.compareTo(o.main.name) : (expr != null && o.expr == null ? 1 : -1)));
    }
}