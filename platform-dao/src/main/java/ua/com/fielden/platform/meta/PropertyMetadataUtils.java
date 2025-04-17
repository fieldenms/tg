package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.persistence.types.MoneyType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountType;
import ua.com.fielden.platform.types.Money;

import java.util.List;
import java.util.function.Predicate;

public interface PropertyMetadataUtils {

    /**
     * If property type is an entity type that has metadata, tests the metadata with the predicate, otherwise returns {@code false}.
     */
    boolean isPropEntityType(PropertyTypeMetadata propType, Predicate<EntityMetadata> predicate);

    /**
     * If property type is an entity type, tests it with the given predicate, otherwise returns {@code false}.
     */
    default boolean isPropEntityType(final PropertyMetadata pm, final Predicate<EntityMetadata> predicate) {
        return isPropEntityType(pm.type(), predicate);
    }

    /// Equivalent to [#subProperties(PropertyMetadata, SubPropertyNaming)] with [SubPropertyNaming#SIMPLE] naming.
    default List<PropertyMetadata> subProperties(PropertyMetadata pm) {
        return subProperties(pm, SubPropertyNaming.SIMPLE);
    }

    /**
     * Returns sub-properties of a property.
     * The result depends on the property's type and nature.
     *
     * <ul>
     *   <li> Union Entity - all natures are supported and the following sub-properties are included:
     *     <ul>
     *       <li> Union properties (aka union members).
     *       <li> Implicitly calculated properties of the union entity (e.g., common properties).
     *     </ul>
     *   <li> Component Type - depending on the property's nature:
     *     <ul>
     *       <li> Persistent - nature is inherited.
     *       <li> Calculated - nature is inherited, the same expression is used (the effects are unspecified when there are multiple components).
     *       <li> Other natures - makes sense only if the property has a Hibernate type.
     *            Nature is inherited.
     *     </ul>
     *     Note that a component type may have several representations.
     *     Thus, which component properties are included depends on the representation used by the property.
     *     For example, {@link Money} can be represented as: {@link MoneyType}, {@link MoneyWithTaxAmountType}, etc.
     *     Each of these representations is free to decide which properties of {@link Money} are included.
     *   <li> Other types - nothing is included.
     * </ul>
     *
     * @param naming  specifies how to form the names of resulting sub-properties
     */
    List<PropertyMetadata> subProperties(PropertyMetadata pm, SubPropertyNaming naming);

    /// Naming strategies for sub-properties.
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
