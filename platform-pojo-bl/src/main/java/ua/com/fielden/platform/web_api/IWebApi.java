package ua.com.fielden.platform.web_api;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Interface for the TG Web API, which provides a generic application querying capability to easily integrate
/// domain knowledge within or outside of the application.
///
/// Several layers of the Web API are to be supported:
/// 1. Internal server-side API (this interface).
/// 2. Web API resource (to provide integration with external applications via HTTP) at `/api`.
/// 3. Web application UI for exploring queries at `/resources/api/graphiql.html`.
///
/// Please note that the Web API supports _active_ and _memory_ functions beside _informative_
/// (as per "Conceptual Modeling of Information Systems" by Antoni Olivé).
///
/// ### Enhanced entity types
///
/// A user can define entity types at runtime, each extending a domain type with calculated properties, using [#enhanceEntityType].
/// An enhanced entity type is visible only to queries executed by the user who defined it, and only through its own root fields:
/// properties of other types keep their domain types.
/// Enhanced entity types are held in memory, so they do not survive a restart.
///
public interface IWebApi {

    /// Executes a Web API query taking `input` and returning a result in some specific format.
    ///
    /// [WebApiUtils] provides methods for accessing specific components of `input` and the result.
    ///
    /// @param input  a map with the following keys:
    /// - `query : String` -- query in some specific format based on the Web API implementation (required).
    /// - `operationName : String` -- central operation from `query` that is required only if there are multiple ones
    ///   (in which case all of them need to have some name); otherwise empty.
    /// - `variables : Map<String, Object>` -- map of variables for the query.
    ///
    /// @return  a map with the following keys:
    /// - `data` -- JSON-like structured result of the query.
    /// - `errors` -- list of errors that may have occurred during query execution.
    /// - `extensions` -- optional map of extensions that are outside of specification scope.
    ///
    Map<String, Object> execute(Map<String, Object> input);
    
    /// A calculated property to be added to an enhanced entity type.
    ///
    /// @param expression  an expression in TG's expression language, in the context of the enhanced entity type
    ///
    record PropertyDefinition(String name, String expression) {}

    /// The outcome of [#enhanceEntityType].
    ///
    /// @param warnings  messages about parts of the request that were ignored
    ///
    record Enhancement(EnhancedEntityType entityType, List<String> warnings) {}

    /// Creates an enhanced entity type for the current user, or enhances an existing one.
    ///
    /// If the current user has no enhanced entity type named `name`, one is created that extends `baseType` with `addProperties`.
    /// Otherwise, `removeProperties` are removed from that type, and then `addProperties` are added to it.
    /// Properties are added in the specified order, so the expression of each one may refer to those added before it.
    ///
    /// A request either takes full effect or none.
    /// It is rejected if:
    /// - `name` or a property name is not both a Java identifier other than a keyword and a GraphQL name;
    /// - a new type is requested without `baseType`, or `baseType` is not a queryable domain type whose model the user can read;
    /// - `baseType` is specified for an existing type;
    /// - the name of a new type, or a name derived from it, names a type or a root field of the user's GraphQL schema;
    /// - `addProperties` contains a property of the base type or of the existing enhanced type, or the same name twice;
    /// - `removeProperties` contains a property of the base type, or a property from `addProperties`;
    /// - an expression is invalid, or refers to a property that the user cannot read;
    /// - removing a property will break existing references to it from other properties' expressions.
    ///
    /// A property in `removeProperties` that the enhanced entity type does not have is ignored with a warning.
    ///
    /// @param baseType  the GraphQL type name of a domain type, required only to create a new type
    /// @throws WebApiException  if the request is rejected
    ///
    Enhancement enhanceEntityType(String name, @Nullable String baseType, List<PropertyDefinition> addProperties, List<String> removeProperties);

    /// Returns the current user's enhanced entity type named `name`, if there is one.
    ///
    Optional<EnhancedEntityType> findEnhancedEntityType(CharSequence name);

    /// Discards all enhanced entity types of the current user.
    ///
    void discardEnhancedEntityTypes();

}
