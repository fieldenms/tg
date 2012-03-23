package ua.com.fielden.platform.swing.review;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;

/**
 *
 * Panel for radio buttons created with ordinary property editor for the enumeration type property. That panel doesn't add radio buttons directly to it's container. It add radio
 * buttons to map. Then user can set the layout for this panel and add radio buttons directly to this container with layoutEditor method
 *
 * @author oleh
 *
 */
public class RadioButtonPanel extends JPanel {

    private static final long serialVersionUID = 3902307902087152865L;

    // map that holds radio buttons with their enumeration type key
    @SuppressWarnings("unchecked")
    private final Map<Enum, BoundedValidationLayer<JRadioButton>> radioButtonMap;

    /**
     * Creates new radio button panel and creates map for the readio buttons
     */
    @SuppressWarnings("unchecked")
    public RadioButtonPanel() {
	this.radioButtonMap = new HashMap<Enum, BoundedValidationLayer<JRadioButton>>();
    }

    /**
     * Returns the editor for the given enumeration type key
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public BoundedValidationLayer<JRadioButton> getEditorFor(final Enum key) {
	return radioButtonMap.get(key);
    }

    /**
     * Add the editor (i.e. radio button) associated with the enumeration type key to the map
     *
     * @param key
     * @param radioButton
     */
    @SuppressWarnings("unchecked")
    public void addEditor(final Enum key, final BoundedValidationLayer<JRadioButton> radioButton) {
	radioButtonMap.put(key, radioButton);
    }

    /**
     * Add the radio button associated with the key to the container if it's not yet added with the specified constrained for the layout manager
     *
     * @param key
     * @param constrained
     */
    @SuppressWarnings("rawtypes")
    public void layoutEditor(final Enum key, final Object constrained) {
	final BoundedValidationLayer<JRadioButton> value = radioButtonMap.get(key);
	if (value != null) {
	    add(value, constrained);
	}
    }

    /**
     * Add the radio button associated with the key to the container if it's not yet added.
     *
     * @param key
     */
    @SuppressWarnings("rawtypes")
    public void layoutEditor(final Enum key) {
	final BoundedValidationLayer<JRadioButton> value = radioButtonMap.get(key);
	if (value != null) {
	    add(value);
	}
    }

    /**
     * removes the editor associated with the key from the panle's container
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public BoundedValidationLayer<JRadioButton> removeEditor(final Enum key) {
	final BoundedValidationLayer<JRadioButton> value = radioButtonMap.get(key);
	if (value != null) {
	    remove(value);
	    return value;
	}
	return null;
    }

    /**
     * removes the editor (i. e. radio button) from the container and map
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public BoundedValidationLayer<JRadioButton> removeFromMap(final Enum key) {
	removeEditor(key);
	return radioButtonMap.remove(key);
    }

    /**
     * Returns all available radio buttons for this panel.
     *
     * @return
     */
    public Collection<BoundedValidationLayer<JRadioButton>> getEditors() {
	return radioButtonMap.values();
    }
}
