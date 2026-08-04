package ua.com.fielden.platform.eql.stage3;

/// A common interface for all EQL stage 3 AST nodes.
///
/// ## Equality and hash code
///
/// Two notions of equality are defined for stage 3 nodes:
/// 1. **Reference-based equality**.
///    All nodes within an AST are unique Java objects.
///    Therefore, `==` can be used to uniquely identify a node.
/// 2. **Structural equality**.
///    Two nodes are structurally equal iff they have the same type and all of their components are equal.
///    See [StructuralEquivalenceVisitor].
///
///    An extension to this notion is **alpha-equivalence**, which ignores generated identifiers during comparison.
///    See [AlphaEquivalenceVisitor].
///
/// The hash code operation is undefined for stage 3 nodes.
/// Therefore, they should not be stored in hash-based collections.
///
/// Note: "equality" and "equivalence" are used interchangeably in the context of the EQL AST.
///
/// ## Developer notes
///
/// This interface will be sealed at some point, which will require all nodes to be moved to the same package as this interface.
///
public interface INode3 {

    /// Not defined for stage 3 AST nodes.
    /// Concrete nodes inherit the default implementation either from [Object] or [Record].
    ///
    boolean equals(Object o);

    /// Not defined for stage 3 AST nodes.
    /// Concrete nodes inherit the default implementation either from [Object] or [Record].
    ///
    int hashCode();

}
