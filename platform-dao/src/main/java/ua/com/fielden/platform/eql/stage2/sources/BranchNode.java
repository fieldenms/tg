package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;

public class BranchNode {
    public final String name; // name within the holder source; either name or expression is null
    public final Expression2 expr; // expression is based on the holder source

    public final Source2BasedOnPersistentType source;
    public final boolean required;
    private final List<BranchNode> branches; //can contain none
    
    public BranchNode(final String name, final List<BranchNode> branches, final boolean required, final Source2BasedOnPersistentType source, final Expression2 expr) {
        this.name = name;
        this.branches = branches;
        this.required = required;
        this.source = source;
        this.expr = expr;
    }
    
    public List<BranchNode> branches() {
        return unmodifiableList(branches);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + branches.hashCode();
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
                Objects.equals(expr, other.expr);
    }
}