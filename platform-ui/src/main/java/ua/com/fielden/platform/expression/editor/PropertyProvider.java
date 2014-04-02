package ua.com.fielden.platform.expression.editor;

/**
 * Default implementation of the {@link IPropertyProvider}.
 * 
 * @author TG Team
 * 
 */
public class PropertyProvider extends AbstractPropertyProvider {

    @Override
    public void propertyStateChanged(final String propertyName, final boolean isSelect) {
        fireSelectionPropertyEvent(propertyName, isSelect);
    }

}
