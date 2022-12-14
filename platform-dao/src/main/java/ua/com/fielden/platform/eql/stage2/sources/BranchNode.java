package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;

public class BranchNode {
    public final String name;
    public final Source2BasedOnPersistentType source;
    public final boolean required;
    private final List<LeafNode> leaves;
    private final List<BranchNode> branches;
    public final Expression2 expr;
    
    public BranchNode(final String name, final List<LeafNode> leaves, final List<BranchNode> branches, final boolean required, final Source2BasedOnPersistentType source, final Expression2 expr) {
        this.name = name;
        this.leaves = leaves;
        this.branches = branches;
        this.required = required;
        this.source = source;
        this.expr = expr;

//        assert(
//                 items.isEmpty() && source == null && !paths.isEmpty() && (expr == null || expr != null)//
//                || //
//                !items.isEmpty() && source != null && (paths.isEmpty() || !paths.isEmpty()) && (expr == null || expr != null)
//                );
    }
    
    public List<LeafNode> leaves() {
        return unmodifiableList(leaves);
    }

    public List<BranchNode> branches() {
        return unmodifiableList(branches);
    }
    
    @Override
    public String toString() {
        return toString("");
    }

    private static String offset = "              ";
    
    private String toString(final String currentOffset) {
        final StringBuffer sb = new StringBuffer();
//        sb.append("\n" + currentOffset + "**** CHILDGROUP **** name : " + name + (expr != null ? " [CALC]" : ""));
//        if (!paths.isEmpty()) {
//            for (final Prop2Link path : paths) {
//                sb.append("\n" + currentOffset + "-------- absolutePropPath : [" + path.sourceId + "]*[" +path.name+ "]");    
//            }
//        }
//        if (!items.isEmpty()) {
//            sb.append("\n" + currentOffset + "-----------source + items : [" + source.id + "]");
//            for (final BranchNode childGroup : items) {
//                sb.append("\n");
//                sb.append(childGroup.toString(currentOffset + offset));
//            }
//        }
        
        return sb.toString();
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + branches.hashCode();
        result = prime * result + leaves.hashCode();
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BranchNode)) {
            return false;
        }

        final BranchNode other = (BranchNode) obj;
        
        return Objects.equals(name, other.name) && //
                Objects.equals(source, other.source) && //
                Objects.equals(required, other.required) && //
                Objects.equals(branches, other.branches) && //
                Objects.equals(leaves, other.leaves) && //
                Objects.equals(expr, other.expr);
    }
}