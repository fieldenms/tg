/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping;

/**
 * Class that represents mapping from some {@link Boolean} property of some {@linimport ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping; k
 * AbstractEntity} class to {@link EntityGridInspector}'s column. Uses
 * {@link ComponentFactory#createCheckBox(AbstractEntity, String, String, String, ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction...)} method to
 * create bounded {@link JCheckBox}. Methods could be overridden to provide custom functionality.
 *
 * @author Yura
 *
 * @param <T>
 * @param <K>
 */
@SuppressWarnings("unchecked")
public class BoundedBooleanMapping<T extends AbstractEntity> extends ReadonlyPropertyColumnMapping<T> {

    private final IOnCommitAction<T>[] onCommitActions;

    /**
     * Creates instance of mapping, that automatically bounds editors to specified properties of entities in related {@link EntityGridInspector}
     *
     * @param columnName
     * @param prefSize
     * @param headerTooltip
     *            - tooltip of column header
     * @param propertyName
     *            - name of property (property should be of boolean type)
     */
    public BoundedBooleanMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
	super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, true);

	this.onCommitActions = onCommitActions;
    }

    /**
     * Uses {@link ComponentFactory#createCheckBox(AbstractEntity, String, String, String, ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction...)} to
     * create bounded component. Override to provide custom logic
     */
    @Override
    public EditorComponent<BoundedValidationLayer<JCheckBox>, JCheckBox> createBoundedEditorFor(final T entity) {
	final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);
	final BoundedValidationLayer<JCheckBox> layer = ComponentFactory.createCheckBox(entity, getPropertyName(), "", "", onCommitActionWrappers);
	layer.getView().setHorizontalAlignment(SwingConstants.CENTER);
	return new EditorComponent<BoundedValidationLayer<JCheckBox>, JCheckBox>(layer, layer.getView());
    }

    /**
     * Always editable. Override to provide custom logic
     */
    @Override
    public boolean isPropertyEditable(final T entity) {
	return true;
    }

    /**
     * Always navigable. Override to provide custom logic
     */
    @Override
    public boolean isNavigableTo(final T entity) {
	return true;
    }

    /**
     * Should select check box on the first click
     *
     * @param e
     * @return
     */
    @Override
    public boolean startCellEditingOn(final EventObject e) {
	if (e instanceof MouseEvent) {
	    return ((MouseEvent) e).getClickCount() >= 1;
	}
	return true;
    }
}
