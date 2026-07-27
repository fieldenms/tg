package ua.com.fielden.platform.migration;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;
import java.util.SortedMap;

/// Specifies a mapping between data in a source database and a domain entity type.
/// This mapping is then executed to populate a target database.
///
/// A retriever performs either an _insert_ or an _update_ function.
///
/// The standard way to implement a retriever is to extend [AbstractRetriever].
///
/// @param <T>  entity type to be populated in a target database
///
public interface IRetriever<T extends AbstractEntity<?>> {

    /// A map, where keys are property path in entity type `T`, and values are SQL expressions to be executed in a source database.
    ///
    /// For an insert retriever, all required properties of `T` must be specified.
    /// For an update retriever, all key members must be specified.
    ///
    /// Here are the rules for specifying properties:
    /// * **Primitive properties** are the simplest. The map key must be a property name.
    /// * **Entity-typed properties**: depends on the key type of a corresponding entity type.
    ///   * For simple keys, the map key must be the name of the entity-typed property.
    ///
    ///     E.g., `vehicle`, rather than `vehicle.key`.
    ///   * For composite keys, the whole structure of the key must be specified.
    ///
    ///     E.g., for `Vehicle.key : { number : String, model : Model }; Model.key : String`, the map must contain 2 keys:
    ///     `number`, `model`.
    /// * **Component-typed properties**: each component must be specified.
    ///   However, when there is just one component, the property must be specified by itself (e.g., `money` instead of `money.amount`).
    /// * **Union-typed properties**: each union member must be specified according to the rules for specifying entity-typed properties.
    ///
    SortedMap<String, String> resultFields();

    /// SQL expression that will be used in the `FROM` clause.
    /// This may be a table name, a `SELECT`, or any other valid SQL expression.
    ///
    String fromSql();

    /// Optional SQL expression that will be used in the `WHERE` clause.
    ///
    @Nullable String whereSql();

    /// Optional SQL expression that will be used in the `GROUP BY` clause.
    ///
    @Nullable List<String> groupSql();

    /// Optional SQL expression that will be used in the `GROUP BY` clause.
    ///
    @Nullable List<String> orderSql();

    /// Type of the entity associated with this retriever.
    ///
    Class<T> type();

    boolean isUpdater();

}
