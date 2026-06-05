package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.persistence.types.MoneyType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountType;
import ua.com.fielden.platform.types.Money;

import java.util.List;
import java.util.function.Predicate;

public interface PropertyMetadataUtils {

    /// If property type is an entity type that has metadata, tests the metadata with the predicate, otherwise returns `false`.
    ///
    boolean isPropEntityType(PropertyTypeMetadata propType, Predicate<EntityMetadata> predicate);

    /// If property type is an entity type, tests it with the given predicate, otherwise returns `false`.
    ///
    default boolean isPropEntityType(final PropertyMetadata pm, final Predicate<EntityMetadata> predicate) {
        return isPropEntityType(pm.type(), predicate);
    }

    /// Equivalent to [#subProperties(PropertyMetadata, SubPropertyNaming)] with [SubPropertyNaming#SIMPLE] naming.
    ///
    default List<PropertyMetadata> subProperties(final PropertyMetadata pm) {
        return subProperties(pm, SubPropertyNaming.SIMPLE);
    }

    /// Returns sub-properties of a property.
    /// The result depends on the property’s type and nature.
    ///
    /// * **Union Entity** – all natures are supported, and the following sub-properties are included:
    ///   * Union properties (union members).
    ///   * Implicitly calculated properties of the union entity (e.g. common properties).
    ///
    /// * **Component Type** – behaviour depends on the property’s nature:
    ///   * _Persistent_ – nature is inherited.
    ///   * _Calculated_ – nature is inherited and the same expression is used
    ///     (the effects are unspecified if there are multiple components).
    ///   * _Other natures_ – only meaningful if the property has a Hibernate type;
    ///     the nature is inherited.
    ///
    ///   A component type may have several representations, so which component properties are included
    ///   depends on the representation used by the property.
    ///   For example, [Money] can be represented as [MoneyType], [MoneyWithTaxAmountType], etc., and each
    ///   representation may choose which [Money] properties to expose.
    ///
    /// * **Other types** – nothing is included.
    ///
    /// @param naming  specifies how to form the names of the resulting sub-properties
    ///
    List<PropertyMetadata> subProperties(PropertyMetadata pm, SubPropertyNaming naming);

    /// Naming strategies for sub-properties.
    ///
    enum SubPropertyNaming {

        /// The resulting name is the simple name of the sub-property.
        SIMPLE {
            @Override
            public String apply(final String propName, final String subPropName) {
                return subPropName;
            }
        },

        /// The resulting name is a path that ends with the sub-property's name.
        PATH {
            @Override
            public String apply(final String propName, final String subPropName) {
                return propName + '.' + subPropName;
            }
        };

        public abstract String apply(final String propName, final String subPropName);

    }

}
