package ua.com.fielden.platform.swing.review;


public interface IPropertyListChangedListener {

    void propertyRemoved(final String key, DynamicProperty proerty, String propertyName);

    void propertyAdded(final String key, final DynamicProperty dynamicProperty, String propertyName);
}
