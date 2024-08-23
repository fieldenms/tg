package ua.com.fielden.platform.commonmark;

import com.google.common.collect.Streams;
import org.commonmark.node.Node;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Extensions and utilities for the <a href="https://github.com/commonmark/commonmark-java">commonmark-java</a> library.
 */
public final class CommonMark {

    public static Stream<Node> streamChildren(final Node parent) {
        return Streams.stream(childrenIterator(parent));
    }

    public static Iterator<Node> childrenIterator(final Node parent) {
        return new ChildrenIterator(parent);
    }

    private CommonMark() {}

}
