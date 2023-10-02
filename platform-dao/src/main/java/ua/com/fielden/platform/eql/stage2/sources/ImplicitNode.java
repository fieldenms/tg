package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class ImplicitNode {
    public final String name; // name within the holder source; either name or expression is null TODO currently name is always provided
    public final Expression2 expr; // expression is based on the holder source
    public final boolean nonnullable;

    public final ISource2<? extends ISource3> source;
    private final List<ImplicitNode> subnodes; //can contain none
    
    public ImplicitNode(final String name, final Expression2 expr, final boolean nonnullable, final ISource2<? extends ISource3> source, final List<ImplicitNode> subnodes) {
        this.name = name;
        this.expr = expr;
        this.nonnullable = nonnullable;
        this.source = source;
        this.subnodes = subnodes;
    }
    
    public List<ImplicitNode> subnodes() {
        return unmodifiableList(subnodes);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (nonnullable ? 1231 : 1237);
        result = prime * result + subnodes.hashCode();
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ImplicitNode)) {
            return false;
        }

        final ImplicitNode other = (ImplicitNode) obj;
        
        return Objects.equals(name, other.name) && //
                Objects.equals(expr, other.expr) && //
                Objects.equals(nonnullable, other.nonnullable) && //
                Objects.equals(source, other.source) && //
                Objects.equals(subnodes, other.subnodes);
    }
}