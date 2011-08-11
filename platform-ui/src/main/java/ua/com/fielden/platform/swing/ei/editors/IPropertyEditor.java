package ua.com.fielden.platform.swing.ei.editors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for property editor, which consists of a label and an editor component. These components are accessible individually to support custom layout. Also, there is a
 * default layout, which can be used when convenient.
 * 
 * @author 01es
 * 
 */
public interface IPropertyEditor {
    /**
     * Returns an entity instance bound the editor.
     * 
     * @return
     */
    AbstractEntity<?> getEntity();

    /**
     * Returns a property name bound to the editor.
     * 
     * @return
     */
    String getPropertyName();

    /**
     * Bind an entity instance to an editor that could already exist.
     * 
     * @param entity
     */
    void bind(final AbstractEntity<?> entity);

    /**
     * Provides access to a component representing property label.
     * 
     * @return
     */
    JLabel getLabel();

    /**
     * Provides access to an editor component.
     * 
     * @return
     */
    JComponent getEditor();

    /**
     * Provides a panel with both label and editor components stacked next to each other (label: editor).
     * 
     * @return
     */
    JPanel getDefaultLayout();

    /**
     * Provides access to an instance of a value matcher. All editors for properties with an entity type should have a corresponding value matcher. Otherwise, it should be null --
     * accessing this method in such cases should throw an exception.
     * 
     * @return
     */
    IValueMatcher<?> getValueMatcher();
    
    /**
     * Returns true if the criterion is empty and should be ignored while constructing a criteria.
     * 
     * @return
     */
    boolean isIgnored();
}
