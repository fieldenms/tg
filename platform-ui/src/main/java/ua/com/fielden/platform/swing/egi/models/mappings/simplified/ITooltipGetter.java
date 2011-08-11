/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

/**
 * Interface that knows how to get tooltips for entities.
 * 
 * @author Yura
 * 
 * @param <T>
 */
public interface ITooltipGetter<T> {

    /**
     * Should return tooltip, related to passed entity, or null if no tooltip exists for that entity
     * 
     * @param entity
     * @return
     */
    public String getTooltip(T entity);

}
