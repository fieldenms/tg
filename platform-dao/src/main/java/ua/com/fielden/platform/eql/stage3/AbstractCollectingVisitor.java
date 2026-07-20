package ua.com.fielden.platform.eql.stage3;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/// Collects every node of type `T` within a tree.
///
/// Concrete visitors must override the [#visit] method corresponding to the node type `T` using the following pattern:
///
/// ```
/// @Override
/// public List<Prop3> visit(final Prop3 x, final Prop3 y, final Void state) {
///     return combine(List.of(x), super.visit(x, y, state));
/// }
/// ```
///
/// A limitation of this visitor is that it must be specialised for each concrete node type `T`.
/// It is not generic in this sense, and cannot be conveniently used given an arbitrary `Class` value.
///
/// The implementation reuses the binary [AbstractSameShapeVisitor] as a unary walk by pairing a tree root with
/// itself: all shape checks then hold, so the traversal covers the whole tree once and [#combine] accumulates the matches.
///
/// @param <T>  type of node to collect
///
public abstract class AbstractCollectingVisitor<T> extends AbstractSameShapeVisitor<List<T>, Void> {

    /// Entry point.
    ///
    public List<T> collect(final Object x) {
        return visit(x, x, null);
    }

    @Override
    protected List<T> identity() {
        return ImmutableList.of();
    }

    @Override
    protected List<T> combine(final List<T> a, final List<T> b) {
        return concatList(a, b);
    }

    @Override
    protected List<T> noMatch(final Object x, final Object y, final Void state) {
        return ImmutableList.of();
    }

    @Override
    protected List<T> defaultValue(final Object x, final Object y, final Void state) {
        return ImmutableList.of();
    }

}
