package ua.com.fielden.platform.entity;

/**
 * Defines a contract for providing information about whether parent entity has children or not
 * <p>
 * For example, entity <code>Wagon</code> may have two hierarchy interpretations -- rotable hierarchy or associated work orders. Obviously there can be more interpretations.
 * 
 * @author 01es
 * @author Yura, Oleh (re-factoring)
 * 
 * @param <P>
 *            The parent entity type
 */
public interface IHierarchyProvider<P> {
    /**
     * Should return true if passed parent entity has at least one child
     * 
     * @param parentEntity
     *            -- instance of the parent entity for which children are obtained
     * @return
     */
    boolean hasChildren(final P parentEntity);
}
