package ua.com.fielden.platform.treemodel;


/**
 * Contract for anything that wants to save some parameter for dot-notation property name.
 * 
 * @author oleh
 * 
 * @param <T>
 *            - type of the parameter value.
 */
interface ITreeParameterManager<T> {

    /**
     * Returns parameter value for the specified dot-notation proeprtyName.
     * 
     * @param propertyName
     * @return
     */
    T getParameterFor(String propertyName);

    /**
     * Set the parameter value for the specified dot-notation propertyName.
     * 
     * @param propertyName
     * @param parameterValue
     * @throws IllegalArgumentException
     *             - throws when the parameter value can not not be set due to incorrect property name or else.
     */
    void setParameterFor(String propertyName, T parameterValue) throws IllegalArgumentException;

}
