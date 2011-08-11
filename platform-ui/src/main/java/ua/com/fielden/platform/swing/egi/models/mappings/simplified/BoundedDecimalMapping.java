package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.AbstractLabelPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;

/**
 * Mapping for properties of type BigDecimal and Money.
 * 
 * @author 01es, Yura
 * 
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BoundedDecimalMapping<T extends AbstractEntity> extends AbstractLabelPropertyColumnMapping<T> {

    private final IOnCommitAction<T>[] onCommitActions;

    public BoundedDecimalMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
	super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);

	this.onCommitActions = onCommitActions;
    }

    @Override
    public JComponent getCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column) {
	final JComponent renderer = super.getCellRendererComponent(entity, value, isSelected, hasFocus, table, row, column);
	if (renderer instanceof ValidationLayer) {
	    ((ValidationLayer<JLabel>) renderer).getView().setHorizontalAlignment(SwingConstants.RIGHT);
	} else {
	    ((JLabel) renderer).setHorizontalAlignment(SwingConstants.RIGHT);
	}
	return renderer;
    }

    @Override
    public EditorComponent<BoundedValidationLayer<JFormattedTextField>, JFormattedTextField> createBoundedEditorFor(final T entity) {
	final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);
	final BoundedValidationLayer<JFormattedTextField> decimalTextFieldLayer = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, getPropertyName(), true, "money originalToolTip text : is this text ignored?", onCommitActionWrappers);
	decimalTextFieldLayer.getView().addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		    getEntityGridInspector().removeEditor();
		}
	    }
	});
	decimalTextFieldLayer.getView().setHorizontalAlignment(JTextField.RIGHT);
	return new EditorComponent<BoundedValidationLayer<JFormattedTextField>, JFormattedTextField>(decimalTextFieldLayer, decimalTextFieldLayer.getView());
    }

}
