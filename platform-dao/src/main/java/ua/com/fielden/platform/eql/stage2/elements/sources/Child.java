package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class Child {//implements Comparable<Child> {
    public final AbstractPropInfo<?> main;
    
    public final QrySource2BasedOnPersistentType source;
    public final boolean required;
    private final List<Child> items;
    
    public final String fullPath; //not null if given child represents explicit prop that needs resolution 
    public final String explicitSourceId;

    public final Expression2 expr;
    public final Set<String> dependencies; //names of nodes, that should be processed (respective joins should be made) prior to processing current child (its expression) 

    public Child(final AbstractPropInfo<?> main, //
            final List<Child> items, //
            final String fullPath, //
            final boolean required, //
            final QrySource2BasedOnPersistentType source, //
            final Expression2 expr, //
            final String explicitSourceId, //
            final List<Child> dependencies) {
        this.main = main;
        this.items = items;
        this.fullPath = fullPath;
        this.required = required;
        this.source = source;
        this.explicitSourceId = explicitSourceId;
        this.expr = expr;
        this.dependencies = dependencies.stream().map(c -> c.main.name).collect(Collectors.toSet());
        assert(items.isEmpty() || !items.isEmpty() && (source !=null || main instanceof ComponentTypePropInfo || main instanceof UnionTypePropInfo));
        assert(dependencies.isEmpty() ||
               !(main instanceof ComponentTypePropInfo || main instanceof UnionTypePropInfo) && !dependencies.isEmpty() && expr != null
               || (main instanceof ComponentTypePropInfo || main instanceof UnionTypePropInfo) && !dependencies.isEmpty() && expr == null);
//        if (source == null && fullPath == null && !isUnionEntityType(main.javaType())) {
//          //  throw new EqlException("Incorrect state.");
//        }
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
        result = prime * result + (explicitSourceId == null ? 0 :explicitSourceId.hashCode());
        //result = prime * result + dependencies.hashCode();
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
        sb.append("\n" + currentOffset + "---- explicitSource : " + explicitSourceId);
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
        
        return Objects.equals(main, other.main) && Objects.equals(items, other.items) && Objects.equals(fullPath, other.fullPath) && Objects.equals(source, other.source) && Objects.equals(explicitSourceId, other.explicitSourceId);// && Objects.equals(dependencies, other.dependencies);
    }
}