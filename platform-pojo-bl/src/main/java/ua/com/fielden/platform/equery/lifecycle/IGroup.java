package ua.com.fielden.platform.equery.lifecycle;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;

/**
 * Interface for lifecycle distribution group.
 *
 * @author TG Team
 *
 * @param <T> - indicates an entity type.
 */
public interface IGroup <T extends AbstractEntity> {

    /**
     * @return a property by which grouping has been performed. It could be {@link ITimeProperty time-distribution property} or {@link IValueProperty value-distribution property}.
     */
    IProperty getProperty();

    /**
     * @return a distribution value of entity {@link #getProperty() property} common for all entities in this group.
     */
    Object getValue();

    /**
     * @return a parent {@link LifecycleModel} from which grouping has been performed. Could be used for extracting narrowed group's {@link LifecycleModel}.
     */
    LifecycleModel<T> getParent();

    /**
     * @return a narrowed group's {@link LifecycleModel}. Should be lazily initialised.
     */
    LifecycleModel<T> getModel();

    /**
     * Returns durations of categories based on "total" indicator.
     *
     * @param total -- if true - total values should be returned, otherwise - average by count of entities.
     * @return
     */
    List<ValuedInterval> getCategoryDurations(final boolean total);

    /**
     * @return short information describing this group (most likely a key {@link #getValue() value}).
     */
    String getInfo();

    /**
     * Returns true if it is time-distribution group, false otherwise.
     *
     * @return
     */
    boolean isTimeDistributed();

    /**
     * Returns keys of entities in group.
     */
    List<Comparable> getEntityKeys();

    /**
     * Returns count of entities that accumulate group.
     * @return
     */
    int size();

}
