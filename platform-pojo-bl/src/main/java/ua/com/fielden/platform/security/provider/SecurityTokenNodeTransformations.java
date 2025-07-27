package ua.com.fielden.platform.security.provider;

import com.google.common.collect.Streams;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.utils.ImmutableSetUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/// Provides a set of primitives for implementing transformations on security token trees.
///
/// #### Implementation
///
/// All token trees are treated as immutable data structures.
/// Each [SecurityTokenNode], by definition, has a reference to its parent and also its children.
/// To modify a node (e.g., to remove one of its children), a new tree object needs to be created,
/// in which the whole branch containing the node being modified will be replaced.
/// The "whole branch" includes: the node itself, all of its ancestors, and all of its children (recursively).
///
/// This implementation is based on *staging* -- the transformation is divided into 2 stages:
/// 1. A data structure describing the operations to be performed on a tree is built. See [Op].
/// 2. The operations are applied to an input tree.
///
/// @see ISecurityTokenNodeTransformation
public final class SecurityTokenNodeTransformations {

    /// Returns a transformation that combines all specified transformations via function composition.
    ///
    public static ISecurityTokenNodeTransformation compose(final ISecurityTokenNodeTransformation... transformations) {
        return compose(Arrays.asList(transformations));
    }

    /// Returns a transformation that combines all specified transformations via function composition.
    ///
    public static ISecurityTokenNodeTransformation compose(final Iterable<ISecurityTokenNodeTransformation> transformations) {
        return tree -> foldLeft(transformations, tree, (acc, tran) -> tran.transform(acc));
    }

    /// Creates a transformer that relocates token `child` under token `parent`.
    /// If `child` does not exist in a tree, it will be created as a leaf node under `parent`.
    ///
    public static ISecurityTokenNodeTransformation setParentOf(final Class<? extends ISecurityToken> child, final Class<? extends ISecurityToken> parent) {
        return tree -> move(tree, findTokenNode(tree, child).orElseGet(() -> new SecurityTokenNode(child)), getTokenNode(tree, parent));
    }

    /// Generalisation of [#setParentOf(Class, Class)], where multiple `children` tokens are relocated under token `parent`.
    ///
    public static ISecurityTokenNodeTransformation setParentOf(final Iterable<Class<? extends ISecurityToken>> children, final Class<? extends ISecurityToken> parent) {
        // This implementation can be optimised to perform a single traversal of the tree.
        // A list of Operations needs to be built for `children`, and all of them should be applied at the same time at each level of a tree.
        // Conflicting operations should be handled as well.
        return tree -> foldLeft(Streams.stream(children),
                                tree,
                                (acc, child) -> move(acc,
                                                     findTokenNode(acc, child).orElseGet(() -> new SecurityTokenNode(child)),
                                                     getTokenNode(acc, parent)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Implementation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    static final String ERR_TOKEN_NOT_IN_TREE = "Token [%s] is not in the tree.";

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
    ///
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
                    throw noSuchToken(token);
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
    ///
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
                    throw noSuchToken(token);
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
                    throw noSuchToken(token);
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
        return findTokenNode(tree, token).orElseThrow(() -> noSuchToken(token));
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

    private static RuntimeException noSuchToken(final Class<? extends ISecurityToken> token) {
        return new InvalidStateException(ERR_TOKEN_NOT_IN_TREE.formatted(token.getTypeName()));
    }

    /// Describes an operation to be performed on a node.
    ///
    private sealed interface Op {

        /// Adds a node to the children of another node.
        ///
        /// @param add  when applied to node `X`, produces node `Y` such that `parent(Y) = X`
        ///
        record Add (Function<SecurityTokenNode, SecurityTokenNode> add) implements Op {}

        /// Removes a node from the children of another node.
        ///
        /// @param token  the sub-node corresponding to this token should be removed
        ///
        record Remove (Class<? extends ISecurityToken> token) implements Op {}

        /// Updates a node by applying an operation to one of its children.
        ///
        /// @param token  the node corresponding to this token should be updated
        /// @param updateOp  an operation that should be applied to the node identified by `token`
        ///
        record Update (Class<? extends ISecurityToken> token, Op updateOp) implements Op {}

    }

    /// Returns a new node that is equal to `node` but whose parent node is `parent`.
    /// The whole sub-tree rooted at `node` is recreated in the resulting node.
    ///
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
