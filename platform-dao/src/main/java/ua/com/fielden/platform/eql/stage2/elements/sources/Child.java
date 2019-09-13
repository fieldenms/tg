package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;

public class Child implements Comparable<Child> {
    final AbstractPropInfo<?, ?> main;
    final boolean required;
    final Set<Child> items;
    final String fullPath; //not null if given child represents explicit prop that needs resolution 
    final String context; //indicates context for table being joined within main (explicit) table (aka dot.notation being resolved by joining this table)
    
    public Child(final AbstractPropInfo<?, ?> main, final Set<Child> items, final String fullPath, final String context, final boolean required) {
        this.main = main;
        this.items = items;
        this.fullPath = fullPath;
        this.context = context;
        this.required = required;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + ((main == null) ? 0 : main.hashCode());
        result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
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
        
        return Objects.equals(main, other.main) && Objects.equals(items, other.items) && Objects.equals(fullPath, other.fullPath);
    }

    @Override
    public int compareTo(final Child o) {
        return context.compareTo(o.context);
    }
}