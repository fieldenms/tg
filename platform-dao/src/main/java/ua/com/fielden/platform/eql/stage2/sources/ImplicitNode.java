package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * Structure that contains information for the join node that should be created implicitly in order to fulfill dot-notation property support.
 *
 * @author TG Team
 */
public class ImplicitNode {
    /**
     * Name of the property of holder source, to which this implicit node should be joined.
     */
    public final String name;
    /**
     * Expression behind the calculated property of holder source, to which this implicit node should be joined (in case that the property is calculated).
     */
    public final Expression2 expr;
    /**
     * Indicates whether property of holder source, to which this implicit node should be joined, is nonnullable (needed for correct join type).
     */
    public final boolean nonnullable;

    /**
     * Source at stage 2 for implicit join node. It is used during transformation of {@code JoinLeafNode2} into stage 3.
     */
    public final ISource2<? extends ISource3> source;
    /**
     * Implicit join sub-nodes of this implicit join node. Can be empty. They are used during transformation of {@code JoinLeafNode2} into stage 3.
     */
    private final List<ImplicitNode> subnodes;

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