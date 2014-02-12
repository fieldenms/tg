package ua.com.fielden.platform.javafx.dashboard2;

import java.util.List;

import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;

/**
 * An interface that retrieves some global parameters, that is applicable to all dashboard items.
 *
 * @author TG Team
 *
 */
public interface IDashboardParamsGetter {

    /**
     * Returns current global parameters.
     *
     * @return
     */
    List<QueryProperty> getCustomParams();

}
