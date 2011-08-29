package ua.com.fielden.platform.swing.review.wizard.development;

import ua.com.fielden.platform.expression.editor.AbstractPropertyProvider;

/**
 * The model that manages the calculated property selection
 * 
 * @author TG Team
 */
public class CalculatedPropertySelectModel extends AbstractPropertyProvider{

    private String selectedProperty = null;
    private boolean isSelected = false;

    /**
     * Returns the selected calculated property.
     * 
     * @return
     */
    public String getSelectedProperty() {
	return selectedProperty;
    }

    /**
     * Returns the value that indicates whether calculated property is selected or not.
     * 
     * @return
     */
    public boolean isPropertySelected(){
	return isSelected;
    }

    @Override
    public void propertyStateChanged(final String propertyName, final boolean isSelect) {
	this.selectedProperty = propertyName;
	this.isSelected = isSelect;
	fireSelectionPropertyEvent(propertyName, isSelect);
    }

}
