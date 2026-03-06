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
    /// @param property  Either a simple property name or a path to a sub-property of a union-typed property.
    ///
    Optional<CalcPropInfo> maybeExpression(Class<? extends AbstractEntity<?>> entityType, CharSequence property);

}
