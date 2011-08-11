package ua.com.fielden.platform.equery;

/**
 * A contract for determining parameter value (range or single) for a property name.
 * 
 * @author oleh
 * 
 */
public interface IParameterGetter {

    /**
     * Returns {@link IParameter} (range or single) for a property name.
     * 
     * @param propertyName
     * @return
     */
    IParameter getParameter(String propertyName);

}
