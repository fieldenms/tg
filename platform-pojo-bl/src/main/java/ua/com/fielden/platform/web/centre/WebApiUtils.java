package ua.com.fielden.platform.web.centre;

import java.util.List;
import java.util.stream.Collectors;

import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCalculatedAndOfTypes;

/**
 * Utilities for Web API (centre, master) and its implementation.
 * 
 * @author TG Team
 *
 */
public class WebApiUtils {
    
    /**
     * The surrogate title of not yet known configuration. This is used during first time centre loading.
     */
    public static final String UNDEFINED_CONFIG_TITLE = "_______________________undefined";
    /**
     * The surrogate title of centre 'link' configuration. This is used when link with centre parameters opens.
     */
    public static final String LINK_CONFIG_TITLE = "_______________________link";
    public static final String CONFIG_DOES_NOT_EXIST = "Configuration does not exist.";
    public static final String SAVE_MSG = "Please save and try again.";
    public static final String DUPLICATE_SAVE_MSG = "Please duplicate, save and try again.";

    /** Private default constructor to prevent instantiation. */
    private WebApiUtils() {
    }
    
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
    
    /**
     * Returns filtered list of checked properties without aggregation calculated properties ('summary properties' aka 'totals'). 
     * 
     * @param checkedProperties
     * @param managedType
     * @return
     */
    public static List<String> checkedPropertiesWithoutSummaries(final List<String> checkedProperties, final Class<?> managedType) {
        return checkedProperties.stream()
            .filter(checkedProperty -> "".equals(checkedProperty) || !isCalculatedAndOfTypes(managedType, checkedProperty, AGGREGATED_EXPRESSION))
            .collect(Collectors.toList());
    }
    
}
