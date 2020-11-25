package ua.com.fielden.platform.eql.stage2.sources;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;

import java.util.Objects;

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

//        assert(
//                items.isEmpty() && !paths.isEmpty() && source == null && (expr == null || expr != null)//
//                || //
//                !items.isEmpty() && source != null & (paths.isEmpty() || !paths.isEmpty()) && (expr == null || expr != null)
//                );
    }
    
    @Override
    public String toString() {
        return toString("");
    }

    private static String offset = "              ";
    
    private String toString(final String currentOffset) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\n" + currentOffset + "**** CHILDGROUP **** name : " + name + (expr != null ? " [CALC]" : ""));
        if (!paths.isEmpty()) {
            for (Entry<String, String> path : paths.entrySet()) {
                sb.append("\n" + currentOffset + "-------- absolutePropPath : [" + path.getValue() + "]*[" +path.getKey()+ "]");    
            }
        }
        if (!items.isEmpty()) {
            sb.append("\n" + currentOffset + "-----------source + items : [" + source.contextId + "]");
            for (final ChildGroup childGroup : items) {
                sb.append("\n");
                sb.append(childGroup.toString(currentOffset + offset));
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