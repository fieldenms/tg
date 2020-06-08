package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class Child implements Comparable<Child> {
    public final AbstractPropInfo<?> main;
    public final QrySource2BasedOnPersistentType source;
    public final boolean required;
    private final List<Child> items;
    
    public final String fullPath; //not null if given child represents explicit prop that needs resolution 
    public final IQrySource2<?> parentSource;

    public final Expression2 expr;
    public final Set<Child> dependencies;

    final int id;
    
    public Child(final AbstractPropInfo<?> main, final List<Child> items, final String fullPath, final boolean required, final QrySource2BasedOnPersistentType source, final Expression2 expr, final IQrySource2<?> parentSource, final Set<Child> dependencies, final int id) {
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
 //       assert(dependencies.isEmpty() || !dependencies.isEmpty() && expr != null);
        assert(parentSource != null);
        if (source == null && fullPath == null && !isUnionEntityType(main.javaType())) {
          //  throw new EqlException("Incorrect state.");
        }
    }
    
    public List<Child> getItems() {
        return unmodifiableList(items);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + items.hashCode();
        result = prime * result + main.hashCode();
        result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + parentSource.hashCode();
        result = prime * result + dependencies.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return toString("");
    }

    private static String offset = "              ";
    
    private String toString(final String currentOffset) {
        final StringBuffer sb = new StringBuffer();
        sb.append(currentOffset + "**** CHILD ****");//[" + hashCode() + "]");
        sb.append("\n" + currentOffset + "-------------- main : " + main.name);
        sb.append("\n" + currentOffset + "------------ source : " + (source != null ? source : ""));
        sb.append("\n" + currentOffset + "------ parentSource : " + parentSource);
        sb.append("\n" + currentOffset + "---------- fullPath : " + (fullPath != null ? fullPath : ""));
        sb.append("\n" + currentOffset + "-------------- expr : " + (expr != null ? "Y" : ""));
        if (!items.isEmpty()) {
            sb.append("\n" + currentOffset + "------------- items :");
            for (final Child child : items) {
                sb.append("\n");
                sb.append(child.toString(currentOffset + offset));
            }
        }
        
        //sb.append("\n" + currentOffset + "***** END *****");//[" + hashCode() + "]");
        
        return sb.toString();
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
        if (o.equals(this)) {
            return 0;
        }

        return dependsOn(o) ? 1 : 
            (o.dependsOn(this) ? -1 : 
                main.name.equals(o.main.name) ? (id > o.id ? 1 : -1) :
                    (expr != null && o.expr != null || expr == null && o.expr == null ? main.name.compareTo(o.main.name) :
                        (expr != null ? 1 : -1)));
    }
}