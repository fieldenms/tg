package ua.com.fielden.platform.web.view.master.api;

import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.web.view.master.api.MatcherOptions.SHOW_ACTIVE_ONLY_ACTION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig0;

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
     * Returns a list of {@link MatcherOptions} for custom Entity Master matcher, if there is one. Otherwise, returns empty list.
     */
    default List<MatcherOptions> matcherOptionsFor(final String propName) {
        return emptyList();
    }

    /**
     * Indicates whether 'active only' action was deliberately shown by specifying {@link MatcherOptions#SHOW_ACTIVE_ONLY_ACTION} option
     * in {@link IAutocompleterConfig0#withMatcher(Class, ua.com.fielden.platform.web.view.master.api.MatcherOptions, ua.com.fielden.platform.web.view.master.api.MatcherOptions...)} method.
     * 
     * @param property
     * @return
     */
    default boolean isActiveOnlyActionShown(final String property) {
        return matcherOptionsFor(property).contains(SHOW_ACTIVE_ONLY_ACTION);
    }

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

    /**
     * Returns <code>additionalProperties</code> for autocompleter configuration for property <code>propertyName</code>.
     * Returns empty set if property is not entity-typed or if the property was not added to master configuration.
     *
     * @param propertyName
     * @return
     */
    default Set<String> additionalAutocompleterPropertiesFor(final String propertyName) {
        return setOf(); // empty by default
    }

    /**
     * Returns the map between property names and action selector for properties those have associated action.
     *
     * @return
     */
    default Map<String, Class<? extends IEntityMultiActionSelector>> propertyActionSelectors() {
        return new HashMap<>();
    }
}
