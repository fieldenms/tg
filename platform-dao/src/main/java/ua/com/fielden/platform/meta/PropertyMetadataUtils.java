package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.persistence.types.MoneyUserType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountUserType;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyUserType;

import java.util.List;
import java.util.function.Predicate;

public interface PropertyMetadataUtils {

    /**
     * If property type is an entity type, tests it with the given predicate, otherwise returns {@code false}.
     */
    boolean isPropEntityType(PropertyTypeMetadata propType, Predicate<EntityMetadata> predicate);

    /**
     * If property type is an entity type, tests it with the given predicate, otherwise returns {@code false}.
     */
    default boolean isPropEntityType(final PropertyMetadata pm, final Predicate<EntityMetadata> predicate) {
        return isPropEntityType(pm.type(), predicate);
    }

    /**
     * Returns sub-properties of a property. The result depends on the property's type:
     * <ul>
     *   <li> Union Entity - the following sub-properties are included:
     *     <ul>
     *       <li> Union members - persistent nature is attributed to each sub-property.
     *       <li> Implicitly calculated properties of the union entity.
     *     </ul>
     *   <li> Composite Type - component properties are included. Their nature depends on the nature of the given property:
     *     <ul>
     *       <li> Persistent - nature is inherited.
     *       <li> Calculated - nature is inherited, the same expression is used (the effects are unspecified when there
     *            are multiple components).
     *     </ul>
     *     Note that a composite type may have several representations. Thus, which component properties are included
     *     depends on the representation used by the property. For example, {@link Money} can be represented as:
     *     {@link MoneyUserType}, {@link MoneyWithTaxAmountUserType}, etc. Each of these representations is free to
     *     decide which properties of {@link Money} are included.
     *   <li> Other types - nothing is included.
     * </ul>
     */
    List<PropertyMetadata> subProperties(PropertyMetadata pm);

}
