package ua.com.fielden.platform.roa;

/**
 * Lists of supported header attributes in the RESTful communication.
 * 
 * @author 01es
 * 
 */
public enum HttpHeaders {
    INFO("info"), // informational header
    PAGE_NO("page-no"), // current page number as indicated by web application at the server end
    PAGES("pages"), // a total number of pages resulting from the request as provided by web application at the server end
    EXISTS("exists"), // mainly used as an attribute in responses to HEAD requests indicating a resource existence
    COUNT("count"), // contains an integer number indicating result size of some query
    ERROR("error"), // should be used to indicate an error (message) in combination with relevant HTTP response code
    AUTHENTICATION("Autherization"), // used for providing authentication security token
    AUTHORIZED("authorised"), // used for providing authentication security token
    AGGR_VALUES("aggr-values"), // used for providing snappy result-set aggregating values
    STALE("stale")// used for indicating whether entity is stale
    ;

    public final String value;

    HttpHeaders(final String value) {
        this.value = value;
    }
}
