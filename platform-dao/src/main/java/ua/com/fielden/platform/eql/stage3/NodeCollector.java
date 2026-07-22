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
public class NodeCollector extends AbstractSameShapeVisitor<List<Object>, Predicate<?>> {


    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public NodeCollector() {}

    /// Entry point.
    ///
    public List<Object> collect(final Object root, final Predicate<?> pred) {
        return visit(root, root, pred);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> visit(final Object x, final Object y, final Predicate<?> pred) {
        if (((Predicate) pred).test(x)) {
            return combine(ImmutableList.of(x), super.visit(x, y, pred));
        }
        else {
            return super.visit(x, y, pred);
        }
    }

    @Override
    protected List<Object> identity() {
        return ImmutableList.of();
    }

    @Override
    protected List<Object> combine(final List<Object> a, final List<Object> b) {
        return concatList(a, b);
    }

    @Override
    protected List<Object> combine(final Stream<List<Object>> stream) {
        return stream.flatMap(Collection::stream).collect(toImmutableList());
    }

    @Override
    protected List<Object> noMatch(final Object x, final Object y, final Predicate<?> pred) {
        return ImmutableList.of();
    }

    @Override
    protected List<Object> defaultValue(final Object x, final Object y, final Predicate<?> pred) {
        return ImmutableList.of();
    }

}
