package ua.com.fielden.platform.web_api;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utils for composing / decomposing Web API input and result objects.
 * 
 * @author TG Team
 *
 */
public class WebApiUtils {
    static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final String OPERATION_NAME = "operationName";
    
    static final String DATA = "data";
    private static final String ERRORS = "errors";
    
    /////////////////////////////////////////////// DECOMPOSE ///////////////////////////////////////////////
    
    /**
     * Returns query string from Web API input object. See {@link IWebApi#execute(Map)} for more details.
     * 
     * @param input
     * @return
     */
    public static String query(final Map<String, Object> input) {
        return (String) input.get(QUERY);
    }
    
    /**
     * Returns operation name from Web API input object. See {@link IWebApi#execute(Map)} for more details.
     * 
     * @param input
     * @return
     */
    public static Optional<String> operationName(final Map<String, Object> input) {
        return ofNullable(input.get(OPERATION_NAME)).map(String.class::cast);
    }
    
    /**
     * Returns variables from Web API input object. See {@link IWebApi#execute(Map)} for more details.
     * 
     * @param input
     * @return
     */
    public static Map<String, Object> variables(final Map<String, Object> input) {
        return (Map<String, Object>) ofNullable(input.get(VARIABLES)).orElse(linkedMapOf());
    }
    
    /**
     * Returns data from Web API result object. See {@link IWebApi#execute(Map)} for more details.
     * 
     * @param result
     * @return
     */
    public static Map<String, Object> data(final Map<String, Object> result) {
        return (Map<String, Object>) ofNullable(result.get(DATA)).orElse(linkedMapOf());
    }
    
    /**
     * Returns errors from Web API result object. See {@link IWebApi#execute(Map)} for more details.
     * 
     * @param result
     * @return
     */
    public static List<Object> errors(final Map<String, Object> result) {
        return (List<Object>) ofNullable(result.get(ERRORS)).orElse(listOf());
    }
    
    /////////////////////////////////////////////// COMPOSE ///////////////////////////////////////////////
    
    public static Map<String, Object> input(final String query) {
        return input(query, null);
    }
    
    public static Map<String, Object> input(final String query, final Map<String, Object> variables) {
        return linkedMapOf(t2(QUERY, query), t2(VARIABLES, variables));
    }
    
    public static Map<String, Object> inputMult(final String query, final String operationName) {
        return input(query, null, operationName);
    }
    
    public static Map<String, Object> input(final String query, final Map<String, Object> variables, final String operationName) {
        return linkedMapOf(t2(QUERY, query), t2(VARIABLES, variables), t2(OPERATION_NAME, operationName));
    }
    
    public static Map<String, Object> result(final Map<String, Object> data) {
        return linkedMapOf(t2(DATA, data));
    }
    
}