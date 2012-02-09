package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTextField;

import org.jdesktop.jxlayer.JXLayer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.configuration.EntityLocatorDialog;

public class AutocompleterUiWithEntityLocator<T extends AbstractEntity> extends AutocompleterUi {

    /**
     * The associated entity locator dialog.
     */
    private final EntityLocatorDialog<T, ?> entityLocatorDialog;

    /**
     * Properties those holds the information about the autocompleter state.
     */
    private Object selectedString;
    private int startSelectedIndex, endSelectedIndex, previousCaretPosition;

    public AutocompleterUiWithEntityLocator(final EntityLocatorDialog<T, ?> entityLocatorDialog,//
	    final AutocompleterTextFieldLayerWithEntityLocator<T> layer, //
	    final String caption) {
	super(layer, caption);
	this.entityLocatorDialog = entityLocatorDialog;
	entityLocatorDialog.bindToAutocompleter(layer);
	layer.getAutocompleter().getTextComponent().addFocusListener(createComponentFocusListener());
    }

    @SuppressWarnings("unchecked")
    @Override
    public AutocompleterTextFieldLayerWithEntityLocator<T> getLayer() {
	return (AutocompleterTextFieldLayerWithEntityLocator<T>) super.getLayer();
    }

    private FocusListener createComponentFocusListener() {
	return new FocusAdapter() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		final List<T> selectedEntities = entityLocatorDialog.getSelectedEntities();
		if (selectedEntities.size() > 0) {
		    selectedString = getLayer().getAutocompleter().getSelectedHint(selectedEntities, startSelectedIndex, endSelectedIndex, previousCaretPosition);
		    getLayer().getAutocompleter().acceptHint(selectedString);
		    selectedEntities.clear();
		}
	    }
	};
    }

    @Override
    protected void processMouseEvent(final MouseEvent event, final JXLayer<JTextField> layer) {
	if (event.getID() == MouseEvent.MOUSE_CLICKED && (event.getModifiers() & InputEvent.CTRL_MASK) != 0) {
	    getState().ctrlAction(this);
	} else {
	    super.processMouseEvent(event, layer);
	}
    }

    public EntityLocatorDialog<T, ?> getEntityLocatorDialog() {
	return entityLocatorDialog;
    }
}
