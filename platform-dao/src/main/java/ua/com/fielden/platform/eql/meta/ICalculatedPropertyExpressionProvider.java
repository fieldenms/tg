package ua.com.fielden.platform.eql.meta;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

/// Provides access to expressions of calculated properties.
///
@ImplementedBy(DefaultCalculatedPropertyExpressionProvider.class)
public interface ICalculatedPropertyExpressionProvider {

    /// Returns the expression for the specified property if it is calculated.
    ///
    /// Parameter `property` can be one of:
    /// * A simple property name.
    /// * A path to a sub-property of a union-typed property (e.g., `location.workshop`).
    /// * A path to a sub-property of a component-typed property (e.g., `price.amount`).
    ///
    Optional<CalcPropInfo> maybeExpression(Class<? extends AbstractEntity<?>> entityType, CharSequence property);

}
