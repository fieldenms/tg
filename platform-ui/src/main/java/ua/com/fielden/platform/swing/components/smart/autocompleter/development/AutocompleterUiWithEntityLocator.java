package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;

import org.jdesktop.jxlayer.JXLayer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.configuration.EntityLocatorDialog;

public class AutocompleterUiWithEntityLocator<T extends AbstractEntity<?>> extends AutocompleterUi {

    /**
     * The associated entity locator dialog.
     */
    private final EntityLocatorDialog<T, ?> entityLocatorDialog;

    public AutocompleterUiWithEntityLocator(final EntityLocatorDialog<T, ?> entityLocatorDialog,//
            final AutocompleterTextFieldLayerWithEntityLocator<T> layer, //
            final String caption) {
        super(layer, caption);
        this.entityLocatorDialog = entityLocatorDialog;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AutocompleterTextFieldLayerWithEntityLocator<T> getLayer() {
        return (AutocompleterTextFieldLayerWithEntityLocator<T>) super.getLayer();
    }

    @Override
    protected void processMouseEvent(final MouseEvent event, final JXLayer<JTextField> layer) {
        if (event.getID() == MouseEvent.MOUSE_CLICKED && (event.getModifiers() & InputEvent.CTRL_MASK) != 0) {
            getState().ctrlAction(this);
        } else {
            super.processMouseEvent(event, layer);
        }
    }

    /**
     * Returns the binded {@link EntityLocatorDialog} instance.
     * 
     * @return
     */
    public EntityLocatorDialog<T, ?> getEntityLocatorDialog() {
        return entityLocatorDialog;
    }
}
