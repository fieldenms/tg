package ua.com.fielden.platform.basic;


/**
 * this interface must implement each enumeration class in order to represent the property with the radio buttons those must have tool tips and text. If one want's to specified
 * some text for the radio button then toString method must be overridden
 * 
 * @author oleh
 * 
 */
public interface IPropertyEnum{

    /**
     * returns the tool tip text for the choice
     * 
     * @return
     */
    String getTooltip();

}
