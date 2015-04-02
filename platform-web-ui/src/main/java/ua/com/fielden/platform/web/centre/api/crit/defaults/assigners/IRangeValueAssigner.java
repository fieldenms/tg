package ua.com.fielden.platform.web.centre.api.crit.defaults.assigners;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A contract for providing default values for selection criteria of the range kind and type <code>V</code> as their value type.
 * There could be a single implementation of this contract for a specific centre, covering all necessary selection criteria.
 * <p>
 * Methods <code>getFromValue</code> and <code>getToValue</code> may return an empty value, which should be recognised as no value needs to be assigned.
 * <p>
 * The centre context most likely would be useful only in case of context dependent entity centre, where it forms part of some compound master.
 * In such cases, the centre context would only contain an instance of the master entity that could potentially be used by custom logic of defining the default
 * selection criteria values.
 * <p>
 * Any other constituents of the centre context such as selection criteria and selected entities are not really applicable at the time of
 * the default value assigner execution as this would be the time of the very initial centre instantiation and well before the <code>run</code> action is executed.
 *
 *
 * @author TG Team
 *
 * @param <V>
 * @param <T>
 */
public interface IRangeValueAssigner<V, T extends AbstractEntity<?>> {

    /**
     * Accepts an entity centre context and a property name that was used for defining a corresponding selection criterion.
     * <p>
     * May return either a value that needs to be assigned or an empty optional value, which indicates that no value needs to be assigned.
     *
     * @param entity
     * @param name
     * @return
     */
    Optional<V> getFromValue(final CentreContext<T, ?> entity, final String name);


    /**
     * Similar as for {@link #getFromValue(CentreContext, String)}, but for the right value of the range.
     * @param entity
     * @param name
     * @return
     */
    Optional<V> getToValue(final CentreContext<T, ?> entity, final String name);
}
