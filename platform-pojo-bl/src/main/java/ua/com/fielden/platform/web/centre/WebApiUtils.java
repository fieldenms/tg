package ua.com.fielden.platform.web.centre;

/**
 * Contains utilities that for Web API (centre, master) and its implementation.
 * 
 * @author TG Team
 *
 */
public class WebApiUtils {
    
    /**
     * Return DSL representation for property name.
     *
     * @param name
     * @return
     */
    public static String dslName(final String name) {
        return name.equals("") ? "this" : name;
    }

    /**
     * Return domain tree representation for property name.
     *
     * @param name
     * @return
     */
    public static String treeName(final String name) {
        return name.equals("this") ? "" : name;
    }
    
}
