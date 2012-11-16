package ua.com.fielden.platform.swing.review.details;

/**
 * Details contract.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IDetails<T> {

    /**
     * Shows details view for given parameter.
     *
     * @param detailsParam
     */
    void showDetails(T detailsParam);

}
