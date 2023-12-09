package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * A structure that contains information for a join node that should be created implicitly in order to support dot-notated property paths.
 *
 * @author TG Team
 */
public class HelperNodeForImplicitJoins {

    /**
     * A name of the property of the holder source, to which this helper node should be implicitly joined.
     * This property is always entity-typed.
     */
    public final String name;

    /**
     * An expression for a calculated property of the holder source, to which this helper node should be implicitly joined.
     * If property is not calculated, {@code expr} would be {@code null}.
     * If property is calculated, it is always entity-typed.
     */
    public final Expression2 expr;

    /**
     * Indicates whether a property of the holder source, to which this helper node should be implicitly joined, is nonnullable (needed for correct join kind).
     */
    public final boolean nonnullable;

    /**
     * An implicit source at stage 2 that is used as a join node. It is used during the transformation of {@code JoinLeafNode2} to stage 3.
     */
    public final ISource2<? extends ISource3> source;

    /**
     * A list of implicit join sub-nodes of this implicit join node. It can be empty. These sub-nodes used during the transformation of {@code JoinLeafNode2} to stage 3.
     */
    private final List<HelperNodeForImplicitJoins> subnodes;

    public HelperNodeForImplicitJoins(final String name, final Expression2 expr, final boolean nonnullable, final ISource2<? extends ISource3> source, final List<HelperNodeForImplicitJoins> subnodes) {
        this.name = name;
        this.expr = expr;
        this.nonnullable = nonnullable;
        this.source = source;
        this.subnodes = subnodes;
    }

    public List<HelperNodeForImplicitJoins> subnodes() {
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

        if (!(obj instanceof HelperNodeForImplicitJoins)) {
            return false;
        }

        final HelperNodeForImplicitJoins other = (HelperNodeForImplicitJoins) obj;

        return Objects.equals(name, other.name) && //
                Objects.equals(expr, other.expr) && //
                Objects.equals(nonnullable, other.nonnullable) && //
                Objects.equals(source, other.source) && //
                Objects.equals(subnodes, other.subnodes);
    }
}