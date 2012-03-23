package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.AbstractLabelPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;

/**
 * Mapping for properties of type Integer and int.
 *
 * @author 01es
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BoundedIntegerMapping<T extends AbstractEntity> extends AbstractLabelPropertyColumnMapping<T> {

    private final IOnCommitAction<T>[] onCommitActions;

    public BoundedIntegerMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
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
    public EditorComponent<BoundedValidationLayer<JSpinner>, JSpinner> createBoundedEditorFor(final T entity) {
	final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);
	final BoundedValidationLayer<JSpinner> intTextFieldLayer = ComponentFactory.createNumberSpinner(entity, getPropertyName(), true, "integer spinner", 1, onCommitActionWrappers);
	// ComponentFactory.createIntegerTextField(entity, getPropertyName(), true, "integer text field", onCommitActionWrappers);

	//	intTextFieldLayer.getView().setHorizontalAlignment(JTextField.RIGHT);
	return new EditorComponent<BoundedValidationLayer<JSpinner>, JSpinner>(intTextFieldLayer, intTextFieldLayer.getView());
    }

}
