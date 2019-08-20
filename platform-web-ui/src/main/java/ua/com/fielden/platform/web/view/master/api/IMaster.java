package ua.com.fielden.platform.web.view.master.api;

import static java.util.Optional.empty;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;

/**
 *
 * An contract for entity master UI. It should be implemented by classes that represent a specific entity master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMaster<T extends AbstractEntity<?>> {


    /**
     * Entity masters may or may not provide information about specific entity value matchers.
     *
     * @param propName
     * @return
     */
    Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName);

    /**
     * Should be implemented by concrete entity master, returning an instance of IRenderable that is capable of rendering a completer master view.
     *
     * @return
     */
    IRenderable render();

    
    /**
     * Returns action configuration for concrete action kind and its number in that kind's space.
     *
     * @param actionKind
     * @param actionNumber
     * @return
     */
    EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber);
    
    default Optional<WidgetSelector> widgetFor(final String propertyName) {
        return empty();
    }
}
