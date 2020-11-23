package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;

public class Child {
    public final String name;
    
    public final QrySource2BasedOnPersistentType source;
    public final boolean required;
    private final List<Child> items;
    
    public final String fullPath; //not null if given child represents explicit prop that needs resolution 
    public final String explicitSourceId;

    public final Expression2 expr;
    public final Set<String> dependencies; //names of nodes, that should be processed (respective joins should be made) prior to processing current child (its expression) 

    public Child(final String name, //
            final List<Child> items, //
            final String fullPath, //
            final boolean required, //
            final QrySource2BasedOnPersistentType source, //
            final Expression2 expr, //
            final String explicitSourceId, //
            final Set<String> dependencies) {
        this.name = name;
        this.items = items;
        this.fullPath = fullPath;
        this.required = required;
        this.source = source;
        this.explicitSourceId = explicitSourceId;
        this.expr = expr;
        this.dependencies = dependencies;

//        assert(items.isEmpty() && fullPath != null && explicitSourceId != null && source == null && (expr == null && dependencies.isEmpty() || expr != null)//
//                        || //
//                        !items.isEmpty() && source != null & (fullPath == null && explicitSourceId == null || fullPath != null && explicitSourceId != null) && (expr == null && dependencies.isEmpty() || expr != null)
//                        
//                );
    }
    
    public List<Child> getItems() {
        return unmodifiableList(items);
    }
    
    @Override
    public String toString() {
        return toString("");
    }

    private static String offset = "              ";
    
    private String toString(final String currentOffset) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\n" + currentOffset + "**** CHILD **** name : " + name);
        if (explicitSourceId != null && fullPath != null) {
            sb.append("\n" + currentOffset + "--- absolutePropPath : [" + explicitSourceId + "]*[" +fullPath+ "]");    
        }
        if (expr != null) {
            sb.append("\n" + currentOffset + "---expr.dependencies : " + dependencies);
        }
        if (!items.isEmpty()) {
            sb.append("\n" + currentOffset + "------source + items : [" + source.contextId + "]");
            for (final Child child : items) {
                sb.append("\n");
                sb.append(child.toString(currentOffset + offset));
            }
        }
        
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + items.hashCode();
        result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
        result = prime * result + (explicitSourceId == null ? 0 :explicitSourceId.hashCode());
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        result = prime * result + dependencies.hashCode();
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
        
        return Objects.equals(name, other.name) && //
                Objects.equals(source, other.source) && //
                Objects.equals(required, other.required) && //
                Objects.equals(items, other.items) && //
                Objects.equals(fullPath, other.fullPath) && //
                Objects.equals(explicitSourceId, other.explicitSourceId) && //
                Objects.equals(expr, other.expr) && //
                Objects.equals(dependencies, other.dependencies);
    }
}