package ua.com.fielden.platform.security.provider;

import com.google.common.collect.Streams;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.utils.ImmutableSetUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

public final class SecurityTokenNodeTransformations {

    static final String ERR_TOKEN_NOT_IN_TREE = "Token [%s] is not in the tree.";

    /// Creates a transformer that relocates token `child` under token `parent`.
    /// If `child` does not exist in a tree, it will be created as a leaf node under `parent`.
    ///
    public static ISecurityTokenNodeTransformation setParentOf(final Class<? extends ISecurityToken> child, final Class<? extends ISecurityToken> parent) {
        return tree -> move(tree, findTokenNode(tree, child).orElseGet(() -> new SecurityTokenNode(child)), getTokenNode(tree, parent));
    }

    /// Generalisation of [#setParentOf(Class, Class)].
    ///
    public static ISecurityTokenNodeTransformation setParentOf(final Iterable<Class<? extends ISecurityToken>> children, final Class<? extends ISecurityToken> parent) {
        return tree -> foldLeft(Streams.stream(children),
                                tree,
                                (acc, child) -> move(acc,
                                                     findTokenNode(acc, child).orElseGet(() -> new SecurityTokenNode(child)),
                                                     getTokenNode(acc, parent)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Implementation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static SortedSet<SecurityTokenNode> move(final SortedSet<SecurityTokenNode> tree, final SecurityTokenNode node, final SecurityTokenNode newParent) {
        if (node.getSuperTokenNode() == newParent) {
            return tree;
        }
        else {
            final var tmpTree = operateTree(tree, operationToRemove(node));
            return operateTree(tmpTree, operationToAdd(node, newParent));
        }
    }

    private static Op operationToAdd(final SecurityTokenNode node, final SecurityTokenNode newParent) {
        final Op init = new Op.Add(parent -> updateParent(node, parent));
        return foldLeft(Stream.concat(Stream.of(newParent), streamAncestors(newParent)),
                        init,
                        (op, nd) -> new Op.Update(nd.getToken(), op));
    }

    private static Op operationToRemove(final SecurityTokenNode node) {
        final Op init = new Op.Remove(node.getToken());
        return foldLeft(streamAncestors(node),
                        init,
                        (op, nd) -> new Op.Update(nd.getToken(), op));
    }

    /// Applies `operation` to a token tree represented by top-level nodes in `tree`.
    private static SortedSet<SecurityTokenNode> operateTree(final SortedSet<SecurityTokenNode> tree, final Op operation) {
        return switch (operation) {
            case Op.Add(var addF) -> ImmutableSetUtils.insert(tree, addF.apply(null));
            case Op.Remove(var token) ->
                    tree.stream().noneMatch(node -> node.getToken() == token)
                            ? tree
                            : tree.stream()
                                    .filter(node -> node.getToken() != token)
                                    .collect(toImmutableSortedSet(naturalOrder()));
            case Op.Update(var token, var updateOp) -> {
                if (tree.stream().noneMatch(node -> node.getToken() == token)) {
                    throw new InvalidStateException(ERR_TOKEN_NOT_IN_TREE.formatted(token.getTypeName()));
                }
                else {
                    yield tree.stream()
                            .map(node -> node.getToken() == token ? operateNode(node, updateOp, Optional.empty()) : node)
                            .collect(toImmutableSortedSet(naturalOrder()));
                }
            }
        };
    }

    /// Applies `operation` to `node`.
    ///
    /// @param maybeParentNode  the parent of `node`; empty if `node` is a top-level node
    private static SecurityTokenNode operateNode(final SecurityTokenNode node, final Op operation, final Optional<SecurityTokenNode> maybeParentNode) {
        return switch (operation) {
            case Op.Add(var addF) -> {
                final var newNode = new SecurityTokenNode(node.getToken(), maybeParentNode.orElse(null));
                node.getSubTokenNodes().forEach(subNode -> updateParent(subNode, newNode));
                addF.apply(newNode); // Side-effect: `newNode` becomes the parent of the node created by `addF`.
                yield newNode;
            }
            case Op.Remove(var token) -> {
                if (node.getSubTokenNode(token) == null) {
                    throw new InvalidStateException(ERR_TOKEN_NOT_IN_TREE.formatted(token.getTypeName()));
                }

                final var newNode = new SecurityTokenNode(node.getToken(), maybeParentNode.orElse(null));
                node.getSubTokenNodes()
                        .stream()
                        .filter(subNode -> subNode.getToken() != token)
                        .forEach(subNode -> updateParent(subNode, newNode));
                yield newNode;
            }
            case Op.Update(var token, var updateOp) -> {
                if (node.getSubTokenNode(token) == null) {
                    throw new InvalidStateException(ERR_TOKEN_NOT_IN_TREE.formatted(token.getTypeName()));
                }

                final var newNode = new SecurityTokenNode(node.getToken(), maybeParentNode.orElse(null));
                node.getSubTokenNodes().forEach(subNode -> {
                    if (subNode.getToken() == token) {
                        operateNode(subNode, updateOp, Optional.of(newNode));
                    }
                    else {
                        updateParent(subNode, newNode);
                    }
                });
                yield newNode;
            }
        };
    }

    private static SecurityTokenNode getTokenNode(final SortedSet<SecurityTokenNode> tree, final Class<? extends ISecurityToken> token) {
        return findTokenNode(tree, token).orElseThrow(() -> noSuchToken(token, tree));
    }

    private static Optional<SecurityTokenNode> findTokenNode(final SortedSet<SecurityTokenNode> tree, final Class<? extends ISecurityToken> token) {
        return flatten(tree).filter(node -> node.getToken() == token).findFirst();
    }

    private static Stream<SecurityTokenNode> flatten(final SortedSet<SecurityTokenNode> tree) {
        return tree.stream()
                .flatMap(node -> Stream.concat(Stream.of(node), node.getSubTokenNodes().stream().flatMap(SecurityTokenNodeTransformations::flatten)));
    }

    private static Stream<SecurityTokenNode> flatten(final SecurityTokenNode node) {
        return Stream.concat(Stream.of(node), node.getSubTokenNodes().stream().flatMap(SecurityTokenNodeTransformations::flatten));
    }

    private static RuntimeException noSuchToken(final Class<? extends ISecurityToken> first, final SortedSet<SecurityTokenNode> tree) {
        return new InvalidStateException(ERR_TOKEN_NOT_IN_TREE.formatted(first.getTypeName()));
    }

    private sealed interface Op {
        record Add (Function<SecurityTokenNode, SecurityTokenNode> add) implements Op {}
        record Remove (Class<? extends ISecurityToken> token) implements Op {}
        record Update (Class<? extends ISecurityToken> token, Op updateOp) implements Op {}
    }

    /// Returns a new node that is equal to `node` but whose parent node is `parent`.
    /// The whole sub-tree rooted at `node` is recreated in the resulting node.
    private static SecurityTokenNode updateParent(final SecurityTokenNode node, final SecurityTokenNode parent) {
        if (node.getSuperTokenNode() == parent) {
            return node;
        }
        else {
            final var newNode = new SecurityTokenNode(node.getToken(), parent);
            node.getSubTokenNodes().forEach(subNode -> updateParent(subNode, newNode));
            return newNode;
        }
    }

    private static Stream<SecurityTokenNode> streamAncestors(final SecurityTokenNode node) {
        return Stream.iterate(node.getSuperTokenNode(), Objects::nonNull, SecurityTokenNode::getSuperTokenNode);
    }

    private SecurityTokenNodeTransformations() {}

}
