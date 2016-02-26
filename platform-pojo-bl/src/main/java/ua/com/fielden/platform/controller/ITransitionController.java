package ua.com.fielden.platform.controller;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A generic interface, which should be implemented whenever transitional behaviour is desired. For example, various status transitions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ITransitionController<T extends AbstractEntity<?>> {

    List<T> transitionsFor(final T value);

}
