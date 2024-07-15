package ua.com.fielden.platform.web_api;

import java.util.Map;

/**
 * Interface for TG Web API.
 * This is intended to provide generic application quering capability to easily integrate domain knowledge in the inside or outside of the application.
 * <p>
 * Several layers of Web API is to be supported:
 *  1. internal server-side API (this interface);
 *  2. web API resource (to be able to integrate from external applications by means of HTTP requests) -- at '/api';
 *  3. web application UI for exploring queries -- at '/resources/api/graphiql.html'.
 * <p>
 * Please note that quering supports 'active' and 'memory' functions and not only 'informative' (as per "Conceptual Modeling of Information Systems" by Antoni Olivé).
 *  
 * @author TG Team
 *
 */
public interface IWebApi {
    
    /**
     * Executes Web API query taking <code>input</code> and returning result in some specific format.
     * The details of the format is as follows.
     * <p>
     * The value of {@code input} is a map containing:
     * <ul>
     * <li>"query" -- string-based query in some specific format based on Web API implementation (required);</li>
     * <li>"operationName" -- central operation from "query" that is required only if there are multiple ones (in this case all of them need to have some name); otherwise empty;</li>
     * <li>"variables" -- map of optional named variable values for the query.</li>
     * </ul>
     * <p>
     * The returning {@code result} is a map containing:
     * <ul>
     * <li>"data" -- JSON-like structured resultant data of Web API query;</li>
     * <li>"errors" -- list of errors that may have occurred during Web API query execution;</li>
     * <li>"extensions" -- optional map of extensions that are outside of specification scope.</li>
     * </ul>
     */
    Map<String, Object> execute(final Map<String, Object> input);
    
}