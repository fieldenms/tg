/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * This class simplifies implementation of {@link AbstractPropertyColumnMapping} for properties of {@link AbstractEntity} instances. It should be used, when property could be
 * rendered as {@link JLabel} (and it uses bounded labels by default), otherwise {@link AbstractCustomColumnMapping} should be sub-classed instead.
 * 
 * @author Yura
 */
@SuppressWarnings("unchecked")
public abstract class AbstractLabelPropertyColumnMapping<T extends AbstractEntity> extends ReadonlyPropertyColumnMapping<T> {

    public AbstractLabelPropertyColumnMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction) {
	super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, true);
    }

    @Override
    public EditorComponent<? extends ValidationLayer<? extends JComponent>, ? extends JTextComponent> getCellEditor(final T entity) {
	final EditorComponent<? extends ValidationLayer<? extends JComponent>, ? extends JTextComponent> editorComponent = (EditorComponent<? extends ValidationLayer<? extends JComponent>, ? extends JTextComponent>) super.getCellEditor(entity);

	final List<FocusListener> focusListeners = Arrays.asList(editorComponent.getEditorItself().getFocusListeners());
	if (!focusListeners.contains(selectAllOnFocusGainedAdapter)) {
	    editorComponent.getEditorItself().addFocusListener(selectAllOnFocusGainedAdapter);
	}

	return editorComponent;
    }

    /**
     * Always returns false, because without decoration autocompleter looks much more satisfying then with one
     */
    @Override
    public boolean decorateEditor() {
	return false;
    }

    @Override
    public boolean isPropertyEditable(final T entity) {
	return true;
    }

    @Override
    public boolean isNavigableTo(final T entity) {
	return true;
    }

    private static final FocusAdapter selectAllOnFocusGainedAdapter = new FocusAdapter() {
	@Override
	public void focusGained(final FocusEvent e) {
	    // following Runnable instance should be invoked after other events (such as caret positioning for example)
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		public void run() {
		    ((JTextComponent) e.getComponent()).selectAll();
		}
	    });
	}
    };

}
