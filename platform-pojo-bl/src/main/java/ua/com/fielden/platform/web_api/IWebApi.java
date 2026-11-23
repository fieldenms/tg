package ua.com.fielden.platform.web_api;

import java.util.Map;

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
    
}