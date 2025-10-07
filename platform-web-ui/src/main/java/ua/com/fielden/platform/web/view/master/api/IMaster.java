package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/// An contract for entity master UI. It should be implemented by classes that represent a specific entity master.
///
public interface IMaster<T extends AbstractEntity<?>> {


    /// Entity masters may or may not provide information about specific entity value matchers.
    ///
    Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName);

    /// Should be implemented by concrete entity master, returning an instance of IRenderable that is capable of rendering a completer master view.
    ///
    IRenderable render();


    /// Returns action configuration for concrete action kind and its number in that kind's space.
    ///
    EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber);

    /// Returns `additionalProperties` for autocompleter configuration for property `propertyName`.
    /// Returns empty set if property is not entity-typed or if the property was not added to master configuration.
    ///
    default Set<String> additionalAutocompleterPropertiesFor(final String propertyName) {
        return setOf(); // empty by default
    }

    /// Returns the map between property names and action selector for properties those have associated action.
    ///
    default Map<String, Class<? extends IEntityMultiActionSelector>> propertyActionSelectors() {
        return new HashMap<>();
    }

    /// Returns the optional entity type of the autocompleter associated with the specified property name.
    /// If the autocompleter is not of an entity type, an empty optional is returned.
    ///
    default <V extends AbstractEntity<?>> Optional<Class<V>> getAutocompleterAssociatedType(final Class<T> entityType, final String propertyName) {
        final var propertyType = determinePropertyType(entityType, propertyName);
        if (AbstractEntity.class.isAssignableFrom(propertyType)) {
            return Optional.of((Class<V>) propertyType);
        }
        return Optional.empty();
    }
}
