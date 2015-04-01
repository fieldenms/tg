package ua.com.fielden.platform.web.centre.api.default_value;

import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A contract for providing default values for selection criteria of the multi-valued kind and type <code>V</code> as their value type.
 * There could be a single implementation of this contract for a specific centre, covering all necessary selection criteria.
 * <p>
 * Method <code>getValues</code> may return an empty value, which should be recognised as no value needs to be assigned.
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
public interface IMultiValueAssigner<V, T extends AbstractEntity<?>> {

    /**
     * Accepts an entity centre context and a property name that was used for defining a corresponding selection criterion.
     * <p>
     * May return either a list of values that need to be assigned or an empty optional value, which indicates that no values need to be assigned.
     * <p>
     * In case of boolean type, it should return a list of at most two {@link Boolean} values, where the first value represents to <code>IS</code> and the
     * second value the <code>IS NOT</code> parts of a multi-valued boolean selection criterion.
     *
     * @param entity
     * @param name
     * @return
     */
    Optional<List<V>> getValues(final CentreContext<T, ?> entity, final String name);
}
