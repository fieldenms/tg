package ua.com.fielden.platform.eql.stage3;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/// Collects every node within a tree that satifies a predicate.
///
/// The implementation reuses the binary [AbstractSameShapeVisitor] as a unary walk by pairing a tree root with
/// itself: all shape checks then hold, so the traversal covers the whole tree once and [#combine] accumulates the matches.
///
public class NodeCollector extends AbstractSameShapeVisitor<List<INode3>, Predicate<? super INode3>> {

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public NodeCollector() {}

    /// Entry point.
    ///
    public List<INode3> collect(final INode3 root, final Predicate<? super INode3> pred) {
        return visit(root, root, pred);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<INode3> visit(final INode3 x, final INode3 y, final Predicate<? super INode3> pred) {
        if (pred.test(x)) {
            return combine(ImmutableList.of(x), super.visit(x, y, pred));
        }
        else {
            return super.visit(x, y, pred);
        }
    }

    @Override
    protected List<INode3> identity() {
        return ImmutableList.of();
    }

    @Override
    protected List<INode3> combine(final List<INode3> a, final List<INode3> b) {
        return concatList(a, b);
    }

    @Override
    protected List<INode3> combine(final Stream<List<INode3>> stream) {
        return stream.flatMap(Collection::stream).collect(toImmutableList());
    }

    @Override
    protected List<INode3> noMatch(final Object x, final Object y, final Predicate<? super INode3> pred) {
        return ImmutableList.of();
    }

    @Override
    protected List<INode3> defaultValue(final INode3 x, final INode3 y, final Predicate<? super INode3> pred) {
        return ImmutableList.of();
    }

}
